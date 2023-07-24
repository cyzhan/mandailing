package city.roast.filter;

import city.roast.constant.GenericError;
import city.roast.constant.RedisKey;
import city.roast.model.TokenPayload;
import city.roast.model.dto.ResponseDTO;
import city.roast.util.AuthHelper;
import city.roast.util.RedisHelper;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@Log4j2
public class AuthFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    private static final String EMPTY = "";
    private final JWTVerifier verifier;
    private final ObjectMapper objectMapper;
    private final AuthHelper authHelper;
    private final RedisHelper redisHelper;


    public AuthFilter(@Value("${app.auth.secret}") String secret,
                      @Autowired ObjectMapper objectMapper,
                      @Autowired AuthHelper authHelper,
                      @Autowired RedisHelper redisHelper) {
//        log.info("app.auth.secret = {}", secret);
        this.verifier = JWT.require(Algorithm.HMAC256(secret)).build();
        this.objectMapper = objectMapper;
        this.authHelper = authHelper;
        this.redisHelper = redisHelper;
    }

    @Override
    public Mono<ServerResponse> filter(ServerRequest serverRequest, HandlerFunction<ServerResponse> handlerFunction) {
        log.info("auth filter do filter");
        String token = serverRequest.headers().firstHeader("Authorization");
        if (null == token || EMPTY.equals(token)) {
            log.error("Authorization is not provided");
            return ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue(ResponseDTO.error(GenericError.CODE_401));
        }

        DecodedJWT decodedJWT;
        try {
            decodedJWT = verifier.verify(token);
        } catch (JWTVerificationException e) {
//            log.error(e.getMessage(), e);
            return ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue(ResponseDTO.error(GenericError.CODE_401));
        }
        Long userId = decodedJWT.getClaim("userId").asLong();
        return redisHelper.getTemplate().opsForValue().get(RedisKey.loginUser(userId))
                .switchIfEmpty(Mono.just(EMPTY))
                .map(redisValue -> {
                    log.info("redisValue = {}", redisValue);
                    if (redisValue.equals(EMPTY) || !decodedJWT.getSignature().equals(redisValue)){
                        throw new RuntimeException("jwt signature is not equal to the value in redis. userId = " + userId);
                    }
                    byte[] bytes = Base64.getDecoder().decode(decodedJWT.getPayload());
                    String payloadJson = new String(bytes, StandardCharsets.UTF_8);
                    try {
                        return objectMapper.readValue(payloadJson, TokenPayload.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .flatMap(tokenPayload -> {
                    serverRequest.attributes().put(TokenPayload.class.getName(), tokenPayload);
                    return handlerFunction.handle(serverRequest);
                })
                .onErrorResume(Exception.class, e -> {
                    log.error(e.getMessage());
//                    log.error(e.getMessage(), e);
                    return ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue(ResponseDTO.error(GenericError.CODE_401));
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
