package burlap.bag.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.constant.ApiError;
import common.constant.RedisKey;
import common.model.vo.ResponseVO;
import common.model.vo.TokenPayload;
import common.util.AuthHelper;
import common.util.RedisHelper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.ReactiveRedisCallback;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Log4j2
@Component
public class SwapTokenFilter implements GatewayFilter, Ordered {

    private static final String EMPTY = "";
    private final JWTVerifier verifier;
    private final ObjectMapper objectMapper;
    private final RedisHelper redisHelper;
    private final AuthHelper authHelper;
    private final String tokenTTL;
    private final long renew;

    private static final String SWAP_TOKEN_SCRIPT = """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                redis.call('set', KEYS[1], ARGV[2], 'EX', ARGV[3])
                redis.call('set', string.format("%s_temp", KEYS[1]), ARGV[1], 'EX', 10)
                return "1"
            else
                return "0"
            end
            """;

    public SwapTokenFilter(@Value("${app.auth.secret}") String secret,
                           @Value("${app.token.ttl}") String tokenTTL,
                           @Value("${app.token.renew.time}") String renew,
                           @Autowired ObjectMapper objectMapper,
                           @Autowired AuthHelper authHelper,
                           @Autowired RedisHelper redisHelper) {
        this.verifier = JWT.require(Algorithm.HMAC256(secret)).build();
        this.tokenTTL = tokenTTL;
        this.renew = Long.parseLong(renew);
        this.objectMapper = objectMapper;
        this.authHelper = authHelper;
        this.redisHelper = redisHelper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
//        log.info("token = {}", token);

        if (exchange.getRequest().getMethod().compareTo(HttpMethod.GET) != 0){
            response.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED);
            DataBuffer buffer = response.bufferFactory().wrap(toBytes(ResponseVO.error(ApiError.CODE_404)));
            return response.writeWith(Mono.just(buffer));
        }

        if (token == null || token.equals(EMPTY)){
            log.debug("Authorization is not provided");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            DataBuffer buffer = response.bufferFactory().wrap(toBytes(ResponseVO.error(ApiError.CODE_401)));
            return response.writeWith(Mono.just(buffer));
        }

        DecodedJWT decodedJWT;
        TokenPayload tokenPayload;
        try {
            decodedJWT = verifier.verify(token);
            tokenPayload = readTokenPayload(decodedJWT);
        } catch (JWTVerificationException | JsonProcessingException e) {
            log.debug("JWT verified fail");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            DataBuffer buffer = response.bufferFactory().wrap(toBytes(ResponseVO.error(ApiError.CODE_401)));
            return response.writeWith(Mono.just(buffer));
        }

        long now = Instant.now().getEpochSecond();
        long expAt = decodedJWT.getExpiresAtAsInstant().getEpochSecond();
        Long userId = decodedJWT.getClaim("userId").asLong();

        if (expAt - now > renew) {
            //no need swap
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            DataBuffer buffer = response.bufferFactory().wrap(toBytes(ResponseVO.error(ApiError.CODE_1000)));
            return response.writeWith(Mono.just(buffer));
        }

        String newToken = authHelper.generateToken(tokenPayload);
        ByteBuffer byteBufferScript = ByteBuffer.wrap(SWAP_TOKEN_SCRIPT.getBytes());
        final ByteBuffer[] keysAndArgs = new ByteBuffer[4];
        keysAndArgs[0] = ByteBuffer.wrap(RedisKey.loginUser(userId).getBytes());
        keysAndArgs[1] = ByteBuffer.wrap(decodedJWT.getSignature().getBytes());// original token
        keysAndArgs[2] = ByteBuffer.wrap(newToken.split("\\.")[2].getBytes());// new token
        keysAndArgs[3] = ByteBuffer.wrap(tokenTTL.getBytes());

        return redisHelper.getTemplate().execute((ReactiveRedisCallback<Long>) connection -> connection.scriptingCommands()
                        .eval(byteBufferScript, ReturnType.INTEGER, 1, keysAndArgs))
                .collectList()
                .flatMap(list -> {
                    Long result = list.get(0);
                    if (result == 1) {
                        exchange.getResponse().setStatusCode(HttpStatus.OK);
                        byte[] bytes = objectMapper.createObjectNode().put("token", newToken).toString().getBytes(StandardCharsets.UTF_8);
                        DataBuffer buffer = response.bufferFactory().wrap(bytes);
                        return response.writeWith(Mono.just(buffer));
                    } else{
                        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);

                        DataBuffer buffer = response.bufferFactory().wrap(toBytes(ResponseVO.error(ApiError.CODE_1001)));
                        return response.writeWith(Mono.just(buffer));
                    }
                });
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private TokenPayload readTokenPayload(DecodedJWT decodedJWT) throws JsonProcessingException {
        byte[] bytes = Base64.getDecoder().decode(decodedJWT.getPayload());
        String payloadJson = new String(bytes, StandardCharsets.UTF_8);
        return objectMapper.readValue(payloadJson, TokenPayload.class);
    }

    private byte[] toBytes(ResponseVO responseVO) {
        try {
            return objectMapper.writeValueAsString(responseVO).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            return "{\"code\":500,\"msg\":\"internal server error, %s\"}".formatted(e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

}
