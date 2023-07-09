package city.roast.handler;

import city.roast.model.dto.ResponseDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Log4j2
public class SystemHandler {

    public Mono<ServerResponse> version(ServerRequest request){
        return ServerResponse.ok().bodyValue(ResponseDTO.ok());
    }

}
