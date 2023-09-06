package city.roast.config;

import common.constant.ContextKey;
import common.constant.RDBMS;
import lombok.extern.log4j.Log4j2;
import org.springframework.r2dbc.connection.lookup.AbstractRoutingConnectionFactory;
import reactor.core.publisher.Mono;

@Log4j2
public class CoffeeConnectionFactory extends AbstractRoutingConnectionFactory {

    @Override
    protected Mono<Object> determineCurrentLookupKey() {
        return Mono.deferContextual(Mono::just).handle(((contextView, objectSynchronousSink) -> {
            if (contextView.hasKey(ContextKey.DATA_SOURCE)){
                log.debug("data source = {}", contextView.get(ContextKey.DATA_SOURCE).toString());
                objectSynchronousSink.next(contextView.get(ContextKey.DATA_SOURCE));
            }else{
                log.debug("data source not specified, set to primary");
                objectSynchronousSink.next(RDBMS.PRIMARY);
            }
        }));
    }

//    @Override
//    protected Mono<Object> determineCurrentLookupKey() {
//        return Mono.deferContextual(Mono::just).handle(((contextView, objectSynchronousSink) -> {
//
//            if (contextView.hasKey(RDBMS.PRIMARY)){
//                objectSynchronousSink.next(contextView.get(RDBMS.PRIMARY));
//            }else{
//                objectSynchronousSink.next(RDBMS.PRIMARY);
//            }
//        }));
//    }

}
