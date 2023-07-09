package city.roast.config;

import city.roast.model.dto.ResponseDTO;
import city.roast.handler.SystemHandler;
import city.roast.handler.UsersHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@Log4j2
public class RouterConfig {

    private final String CONTEXT_PATH;

    public RouterConfig(@Value("${app.context.path}") String CONTEXT_PATH) {
        this.CONTEXT_PATH = CONTEXT_PATH;
    }

    @Bean
    @Order(1)
    public RouterFunction<ServerResponse> getSystemRouter(SystemHandler handler) {
        RouterFunction<ServerResponse> r = route(GET("/version"), handler::version);
        return route().nest(path(CONTEXT_PATH + "/system"), () -> r).build();
    }

    @Bean
    @Order(1)
    public RouterFunction<ServerResponse> getUsersRouter(UsersHandler handler){
        RouterFunction<ServerResponse> r = route(GET("/id/{id}"), handler::findByID)
                .andRoute(POST(""), handler::batch)
                .andRoute(POST("/tx-test"), handler::txTest)
                .andRoute(GET(""), handler::list);
        return route().nest(path(CONTEXT_PATH + "/users"), () -> r).build();
    }

    @Bean
    @Order
    public RouterFunction<ServerResponse> getNotFoundRouter() {
        RouterFunction<ServerResponse> r = route(GET("/**"),request -> apiNotFound()).
                andRoute(POST("/**"), req -> apiNotFound()).
                andRoute(PUT( "/**"), req -> apiNotFound()).
                andRoute(DELETE( "/**"), req -> apiNotFound()).
                andRoute(PATCH( "/**"), req -> apiNotFound());

        return route().nest(path(CONTEXT_PATH), () -> r).build();
    }

    private Mono<ServerResponse> apiNotFound(){
        return ServerResponse.ok().bodyValue(ResponseDTO.apiNotFound());
    }

}
