package city.roast.handler;


import common.exception.DomainLogicException;
import common.model.vo.ResponseVO;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Log4j2
public class ExceptionHandler {

    public Mono<ServerResponse> handle(Throwable t){
        if (t instanceof DomainLogicException dle){
            if (dle.getMessage() != null && !dle.getMessage().isEmpty()){
                log.info(dle.getMessage());
            }
            return ServerResponse.ok().bodyValue(ResponseVO.error(dle.getError()));
        }
        return ServerResponse.ok().bodyValue(ResponseVO.error(500, t.getMessage()));
    }

}
