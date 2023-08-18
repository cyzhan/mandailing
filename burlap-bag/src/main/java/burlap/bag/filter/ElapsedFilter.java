package burlap.bag.filter;

import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;


@Component
@Log4j2
public class ElapsedFilter implements GatewayFilter, Ordered {

    private static final String ELAPSED_TIME_BEGIN = "elapsedTimeBegin";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getAttributes().put(ELAPSED_TIME_BEGIN, Instant.now().toEpochMilli());
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Long startTime = exchange.getAttribute(ELAPSED_TIME_BEGIN);
            if (startTime != null){
                log.info("{}: {}ms", exchange.getRequest().getURI().getRawPath(), Instant.now().toEpochMilli() - startTime);
            }
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
