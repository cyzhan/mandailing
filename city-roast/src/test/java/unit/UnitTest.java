package unit;

import city.roast.config.AppConfig;
import city.roast.model.entity.User;
import city.roast.util.RedisHelper;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.ReactiveRedisCallback;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

@SpringBootTest(classes = {config.TestConfig.class, AppConfig.class})
@Log4j2
public class UnitTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void reactiveRedisTest1() {
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
    public void reactiveRedisTest2() {
        var redisHelper = applicationContext.getBean(RedisHelper.class);

//        Mono<Boolean> booleanMono = redisHelper.writeAsJson("user1", user);
//        StepVerifier.create(booleanMono)
//                .expectNext(Boolean.TRUE)ˇ
//                .verifyComplete();
    }

    @Test
    public void reactiveRedisTest3() {
        log.info("start");
        var redisHelper = applicationContext.getBean(RedisHelper.class);
        ByteBuffer byteBufferScript = ByteBuffer.wrap("return redis.call('get', KEYS[1])".getBytes());
        redisHelper.getTemplate().execute((ReactiveRedisCallback<Integer>) connection -> connection.scriptingCommands()
                        .eval(byteBufferScript, ReturnType.INTEGER, 1, ByteBuffer.wrap("aaa".getBytes())))
                .collectList()
                .doOnNext(list -> {
                    log.info("value =" + list.get(0));
                }).subscribe();
        log.info("end");
    }

    @Test
    public void reactiveRedisTest4() {
        log.info("start");
        var redisHelper = applicationContext.getBean(RedisHelper.class);
        ByteBuffer byteBufferScript = ByteBuffer.wrap("return redis.call('FCALL','changetoken', 1, KEYS[1], ARGV[1], ARGV[2], ARGV[3])".getBytes());
        final ByteBuffer[] keysAndArgs = new ByteBuffer[4];
        keysAndArgs[0] = ByteBuffer.wrap("aaa".getBytes());
        keysAndArgs[1] = ByteBuffer.wrap("1234".getBytes());
        keysAndArgs[2] = ByteBuffer.wrap("5678".getBytes());
        keysAndArgs[3] = ByteBuffer.wrap("60".getBytes());

        redisHelper.getTemplate().execute((ReactiveRedisCallback<Integer>) connection -> connection.scriptingCommands()
                        .eval(byteBufferScript, ReturnType.INTEGER, 1, keysAndArgs))
                .collectList()
                .doOnNext(list -> {
                    log.info("value =" + list.get(0));
                }).subscribe();
        log.info("end");
    }

    @Test
    public void reactiveRedisTest5() {
        log.info("start");
        String script = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                        redis.call('set', KEYS[1], ARGV[2], 'EX', ARGV[3])
                        redis.call('set', string.format("%s_temp", KEYS[1]), ARGV[1], 'EX', 20)
                        return "1"
                    elseif redis.call('get', string.format("%s_temp", KEYS[1])) == ARGV[1] then
                        return "2"
                    else
                        return "0"
                    end
                """;
        var redisHelper = applicationContext.getBean(RedisHelper.class);
        ByteBuffer byteBufferScript = ByteBuffer.wrap(script.getBytes());
        final ByteBuffer[] keysAndArgs = new ByteBuffer[4];
        keysAndArgs[0] = ByteBuffer.wrap("aaa".getBytes());
        keysAndArgs[1] = ByteBuffer.wrap("1234".getBytes());
        keysAndArgs[2] = ByteBuffer.wrap("5678".getBytes());
        keysAndArgs[3] = ByteBuffer.wrap("60".getBytes());

        redisHelper.getTemplate().execute((ReactiveRedisCallback<Integer>) connection -> connection.scriptingCommands()
                        .eval(byteBufferScript, ReturnType.INTEGER, 1, keysAndArgs))
                .collectList()
                .doOnNext(list -> {
                    log.info("value =" + list.get(0));
                }).subscribe();
        log.info("end");
    }

}
