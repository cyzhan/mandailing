package burlap.bag.filter;

import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Log4j2
@Component
public class SwapTokenFilter implements GatewayFilter, Ordered {

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

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
