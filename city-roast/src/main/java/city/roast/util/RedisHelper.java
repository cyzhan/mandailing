package city.roast.util;

import city.roast.model.vo.LockVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.ReactiveRedisCallback;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Component
@AllArgsConstructor
@Log4j2
public class RedisHelper {

    private static final String LOCK_DELETE_SCRIPT = """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1], ARGV[1])
            else
                return "0"
            end
            """;

    private final ReactiveStringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    public <T> Mono<T> readAs(String key, TypeReference<T> tTypeReference) {
        return redisTemplate.opsForValue().get(key)
                .map(jsonString -> {
                    try {
                        return objectMapper.readValue(jsonString, tTypeReference);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public Mono<Boolean> writeAsJson(String key, Object object){
        try {
            return redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public ReactiveStringRedisTemplate getTemplate(){
        return this.redisTemplate;
    }

    public Mono<LockVO> obtainLock(String key, String valueForRls){
        return this.redisTemplate.opsForValue().setIfAbsent(key, valueForRls, Duration.of(30L, ChronoUnit.SECONDS))
                .map(obtained -> {
                    log.info("is lock obtained: {}", obtained);
                    if (obtained){
                        return LockVO.builder().obtained(obtained).key(key).valueForRls(valueForRls).build();
                    }
                    throw new RuntimeException("try obtain lock fail");
                })
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)))
                .onErrorResume(throwable -> Mono.just(LockVO.builder().key(key).obtained(Boolean.FALSE).build()));
    }


    public Mono<Long> rlsLock(String key, String valueForRls){
        ByteBuffer byteBufferScript = ByteBuffer.wrap(LOCK_DELETE_SCRIPT.getBytes());
        final ByteBuffer[] keysAndArgs = new ByteBuffer[2];
        keysAndArgs[0] = ByteBuffer.wrap(key.getBytes());
        keysAndArgs[1] = ByteBuffer.wrap(valueForRls.getBytes());
        return this.redisTemplate.execute((ReactiveRedisCallback<Long>) connection ->
                        connection.scriptingCommands().eval(byteBufferScript, ReturnType.INTEGER, 1, keysAndArgs))
                .collectList()
                .map(list -> list.get(0));
    }

}
