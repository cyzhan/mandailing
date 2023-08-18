package burlap.bag.config;

import burlap.bag.filter.ElapsedFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator gateWayConfigInfo(RouteLocatorBuilder routeLocatorBuilder, ElapsedFilter elapsedFilter){
        // 构建多个路由routes
        RouteLocatorBuilder.Builder routes = routeLocatorBuilder.routes();
        // 具体路由地址
        routes.route("path_city_roast",r -> r.path("/city-roast/**")
                .filters(f -> f.filter(elapsedFilter))
//                .filters(f -> f.stripPrefix(1).filter(elapsedFilter))
                .uri("http://localhost:8080/city-roast/")).build();
        // 返回所有路由规则
        return routes.build();
    }

}
