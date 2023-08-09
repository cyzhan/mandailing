package city.roast.handler;

import city.roast.model.vo.ResponseVO;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
@Log4j2
public class DemoHandler {

    public Mono<ServerResponse> verifyToken(ServerRequest request){
        String newToken = (String) request.attributes().get("newToken");
        if (newToken != null){
            log.info("newToken = " + newToken);
            return ServerResponse.ok().bodyValue(ResponseVO.of(newToken));
        }
        return ServerResponse.ok().bodyValue(ResponseVO.ok());
    }

}
