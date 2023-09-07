package city.roast.handler;


import common.exception.DomainLogicException;
import common.model.vo.ObjectWrapper;

import jakarta.validation.ValidationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
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
            return ServerResponse.ok().bodyValue(ObjectWrapper.error(dle.getApiError()));
        }else if (t instanceof ValidationException ve){
            return ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(ObjectWrapper.error(400, ve.getMessage()));
        }

        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(ObjectWrapper.error(500, t.getMessage()));
    }

}
