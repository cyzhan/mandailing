package burlap.bag.config;

import burlap.bag.filter.AuthFilter;
import burlap.bag.filter.ElapsedFilter;
import burlap.bag.filter.SwapTokenFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import java.lang.reflect.Method;

@Configuration
@ComponentScan(value = {"common.util"})
public class GatewayConfig {

    @Bean
    public RouteLocator gateWayConfigInfo(RouteLocatorBuilder routeLocatorBuilder,
                                          ElapsedFilter elapsedFilter,
                                          AuthFilter authFilter,
                                          SwapTokenFilter swapTokenFilter){
        // 构建多个路由routes
        RouteLocatorBuilder.Builder routes = routeLocatorBuilder.routes();

        // 具体路由地址
        routes.route("city_roast_token_swap", r -> r.path("/city-roast/token-swap")
                .and().method(HttpMethod.GET)
                .filters(f -> f.filters(swapTokenFilter)).uri("http://localhost:8080/")).build();

        routes.route("city_roast_system", r -> r.path( "/city-roast/system/**")
                .filters(f -> f.filters(elapsedFilter))
                .uri("http://localhost:8080/")).build();

        routes.route("city_roast_auth_free", r -> r.path("/city-roast/users/login", "/city-roast/users")
                .and().method(HttpMethod.POST)
                .filters(f -> f.filters(elapsedFilter))
                .uri("http://localhost:8080/")).build();

        routes.route("city_roast_auth",r -> r.path("/city-roast/**")
                .filters(f -> f.filters(authFilter, elapsedFilter))
//                .filters(f -> f.stripPrefix(1).filter(elapsedFilter))
                .uri("http://localhost:8080/city-roast/")).build();
        // 返回所有路由规则
        return routes.build();
    }

}
