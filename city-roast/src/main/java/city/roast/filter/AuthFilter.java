package city.roast.filter;

import common.constant.Error;
import city.roast.constant.RedisKey;
import city.roast.model.TokenPayload;

import city.roast.util.AuthHelper;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.model.vo.ResponseVO;
import common.util.RedisHelper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.ReactiveRedisCallback;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Component
@Log4j2
public class AuthFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    private static final String EMPTY = "";
    private final JWTVerifier verifier;
    private final ObjectMapper objectMapper;
    private final AuthHelper authHelper;
    private final RedisHelper redisHelper;
    private final long renew;
    private static final String SWAP_TOKEN_SCRIPT = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                        redis.call('set', KEYS[1], ARGV[2], 'EX', ARGV[3])
                        redis.call('set', string.format("%s_temp", KEYS[1]), ARGV[1], 'EX', 10)
                        return "1"
                    elseif redis.call('get', string.format("%s_temp", KEYS[1])) == ARGV[1] then
                        return "2"
                    else
                        return "0"
                    end
                """;


    public AuthFilter(@Value("${app.auth.secret}") String secret,
                      @Value("${app.token.renew.time}") String renew,
                      @Autowired ObjectMapper objectMapper,
                      @Autowired AuthHelper authHelper,
                      @Autowired RedisHelper redisHelper) {
        this.renew = Long.parseLong(renew);
        this.verifier = JWT.require(Algorithm.HMAC256(secret)).build();
        this.objectMapper = objectMapper;
        this.authHelper = authHelper;
        this.redisHelper = redisHelper;
    }

    private TokenPayload readTokenPayload(DecodedJWT decodedJWT) throws JsonProcessingException {
        byte[] bytes = Base64.getDecoder().decode(decodedJWT.getPayload());
        String payloadJson = new String(bytes, StandardCharsets.UTF_8);
        return objectMapper.readValue(payloadJson, TokenPayload.class);
    }

    @Override
    public Mono<ServerResponse> filter(ServerRequest serverRequest, HandlerFunction<ServerResponse> handlerFunction) {
        log.debug("auth filter do filter");
        String token = serverRequest.headers().firstHeader("Authorization");
        if (null == token || EMPTY.equals(token)) {
            log.error("Authorization is not provided");
            return ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue(ResponseVO.error(Error.CODE_401));
        }

        DecodedJWT decodedJWT;
        TokenPayload tokenPayload;
        try {
            decodedJWT = verifier.verify(token);
            tokenPayload = readTokenPayload(decodedJWT);
        } catch (JWTVerificationException | JsonProcessingException e) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue(ResponseVO.error(Error.CODE_401));
        }

        long now = Instant.now().getEpochSecond();
        long expAt = decodedJWT.getExpiresAtAsInstant().getEpochSecond();
        Long userId = decodedJWT.getClaim("userId").asLong();
        if (expAt - now > renew) {
            //normal procedure
            return redisHelper.getTemplate().opsForValue().get(RedisKey.loginUser(userId))
                    .switchIfEmpty(Mono.just(EMPTY))
                    .flatMap(redisValue -> {
                        log.debug("redisValue = {}", redisValue);
                        if (redisValue.equals(EMPTY) || !decodedJWT.getSignature().equals(redisValue)) {
                            throw new RuntimeException("jwt signature is not equal to the value in redis. userId = " + userId);
                        }
                        serverRequest.attributes().put(TokenPayload.class.getName(), tokenPayload);
                        return handlerFunction.handle(serverRequest);
                    })
                    .onErrorResume(Exception.class, e -> {
                        log.error(e.getMessage());
                        return ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue(ResponseVO.error(Error.CODE_401));
                    });
        }

        String newToken = authHelper.generateToken(tokenPayload);
        ByteBuffer byteBufferScript = ByteBuffer.wrap(SWAP_TOKEN_SCRIPT.getBytes());
        final ByteBuffer[] keysAndArgs = new ByteBuffer[4];
        keysAndArgs[0] = ByteBuffer.wrap(RedisKey.loginUser(userId).getBytes());
        keysAndArgs[1] = ByteBuffer.wrap(decodedJWT.getSignature().getBytes());
        keysAndArgs[2] = ByteBuffer.wrap(newToken.split("\\.")[2].getBytes());
        keysAndArgs[3] = ByteBuffer.wrap("60".getBytes());

        return redisHelper.getTemplate().execute((ReactiveRedisCallback<Long>) connection -> connection.scriptingCommands()
                        .eval(byteBufferScript, ReturnType.INTEGER, 1, keysAndArgs))
                .collectList()
                .flatMap(list -> {
                    Long result = list.get(0);
                    if (result == 1) {
                        serverRequest.attributes().put("newToken", newToken);
                        return handlerFunction.handle(serverRequest);
                    } else if (result == 2) {
                        return handlerFunction.handle(serverRequest);
                    }
                    return ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue(ResponseVO.error(Error.CODE_401));
                })
                .onErrorResume(Exception.class, e -> {
                    log.error(e.getMessage());
                    return ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue(ResponseVO.error(Error.CODE_401));
                });
    }


//    private boolean isTooManyRequest(int userId){
//        Long count = redisTemplate.opsForValue().increment(RedisKey.requestCount(userId));
//        if (count != null && count == 1){
//            redisTemplate.expire(RedisKey.requestCount(userId), 30, TimeUnit.SECONDS);
//        }
//        return Objects.requireNonNull(count) > 5*30;
//    }

}
