package city.roast.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class RedisHelper {

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

}
