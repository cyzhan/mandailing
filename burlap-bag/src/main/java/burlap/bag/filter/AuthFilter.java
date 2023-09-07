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
import common.model.vo.ObjectWrapper;
import common.model.vo.TokenPayload;
import common.util.RedisHelper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Log4j2
@Component
public class AuthFilter implements GatewayFilter, Ordered {

    private static final String EMPTY = "";
    private final JWTVerifier verifier;
    private final ObjectMapper objectMapper;
    private final RedisHelper redisHelper;

    public AuthFilter(@Value("${app.auth.secret}") String secret,
                      @Autowired ObjectMapper objectMapper,
                      @Autowired RedisHelper redisHelper) {
        log.info("app.auth.secret = {}", secret);
        this.verifier = JWT.require(Algorithm.HMAC256(secret)).build();;
        this.objectMapper = objectMapper;
        this.redisHelper = redisHelper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
//        log.info("token = {}", token);

        if (token == null || token.equals(EMPTY)){
            log.debug("Authorization is not provided");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            DataBuffer buffer = response.bufferFactory().wrap(toBytes(ObjectWrapper.error(ApiError.CODE_401)));
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
            DataBuffer buffer = response.bufferFactory().wrap(toBytes(ObjectWrapper.error(ApiError.CODE_401)));
            return response.writeWith(Mono.just(buffer));
        }
        List<String> keys = List.of(RedisKey.loginUser(tokenPayload.getUserId()), RedisKey.loginUser(tokenPayload.getUserId()) + "_temp");
        return redisHelper.getTemplate().opsForValue().multiGet(keys)
                .flatMap(values -> {
                    if (!values.contains(decodedJWT.getSignature())){
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        DataBuffer buffer = response.bufferFactory().wrap(toBytes(ObjectWrapper.error(ApiError.CODE_401)));
                        return response.writeWith(Mono.just(buffer));
                    }
                    return chain.filter(exchange);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private TokenPayload readTokenPayload(DecodedJWT decodedJWT) throws JsonProcessingException {
        byte[] bytes = Base64.getDecoder().decode(decodedJWT.getPayload());
        String payloadJson = new String(bytes, StandardCharsets.UTF_8);
        return objectMapper.readValue(payloadJson, TokenPayload.class);
    }

    private byte[] toBytes(ObjectWrapper objectWrapper) {
        try {
            return objectMapper.writeValueAsString(objectWrapper).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            return "{\"code\":500,\"msg\":\"internal server error, %s\"}".formatted(e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

}
