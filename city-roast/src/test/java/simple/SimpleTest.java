package simple;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.function.Function;

@Log4j2
public class SimpleTest {

    @Test
    public void jwtTest(){
        String secret = "k8S5F8I@9hOf%cJW1RJjiA0OTa7aXHHbpJt1UYUH06IZJBl7AMY92Dqolex%1^y";
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm)
//                .withIssuer("Baeldung")
                .build();

        Instant instant = Instant.now();

        String jwtToken = JWT.create()
                .withIssuer("Baeldung")
                .withSubject("Baeldung Details")
                .withIssuedAt(instant)
                .withExpiresAt(instant.plus(60L, ChronoUnit.SECONDS))
                .withClaim("userId", "1234")
                .withClaim("name", "hello")

//                .withClaim("createdAt", 13249941897L)
//                .withIssuedAt(new Date())
//                .withExpiresAt(new Date(System.currentTimeMillis() + 5000L))
//                .withJWTId(UUID.randomUUID()
//                        .toString())
//                .withNotBefore(new Date(System.currentTimeMillis() + 1000L))
                .sign(algorithm);

        log.info("jwt = {}", jwtToken);
        DecodedJWT decodedJWT = verifier.verify(jwtToken);
        log.info("do decoded...");
        log.info("payload = {}", decodedJWT.getPayload());
        log.info("userId = {}", decodedJWT.getClaim("userId"));
        log.info("name = {}", decodedJWT.getClaim("name"));
        log.info("signature = {}", decodedJWT.getSignature());
        log.info("issuedAt = {}, expiredAt = {}, diff = {}",
                decodedJWT.getIssuedAt(),
                decodedJWT.getExpiresAt(),
                decodedJWT.getIssuedAt().getTime() - decodedJWT.getExpiresAt().getTime());
    }

    @Test
    public void reactiveTest(){
        Function<Mono<String>, Mono<String>> transform = f -> {
            return f.flatMap(s -> Mono.just(s.toUpperCase(Locale.ROOT)));
        };

        Mono.just("first")
                .doOnNext(System.out::println)
                .transform(transform)
                .log()
                .subscribe(System.out::println);

        try {
            Thread.sleep(1500L);
            log.info("end");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
