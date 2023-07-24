package unit;

import city.roast.model.entity.User;
import city.roast.util.RedisHelper;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

@SpringBootTest(classes = {config.TestConfig.class})
@Log4j2
public class UnitTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void reactiveRedisTest1(){
        var redisHelper = applicationContext.getBean(RedisHelper.class);
        User user = User.builder()
                .id(122L).name("Bob")
                .password("asdf")
                .email("jaksdkfri")
                .balance(new BigDecimal("1.00"))
                .build();
        Mono<Boolean> booleanMono = redisHelper.writeAsJson("user1", user);
        StepVerifier.create(booleanMono)
                .expectNext(Boolean.TRUE)
                .verifyComplete();
    }

    @Test
    public void reactiveRedisTest2(){
        var redisHelper = applicationContext.getBean(RedisHelper.class);
        redisHelper.getTemplate().opsForValue().get("hello")
                .switchIfEmpty(Mono.just("None"))
                .doOnNext(s -> {
                    log.info("s = {}", s);
                }).subscribe(s -> {
                    log.info("subscribe s = {}", s);
                });
//        Mono<Boolean> booleanMono = redisHelper.writeAsJson("user1", user);
//        StepVerifier.create(booleanMono)
//                .expectNext(Boolean.TRUE)
//                .verifyComplete();
    }

}
