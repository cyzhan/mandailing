package config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

//To avoid include config that contain annotations like @EnableScheduling, @EnableKafka
@SpringBootApplication
@EnableR2dbcRepositories(basePackages = {"city.roast.repository"})
@ComponentScan(value = {"city.roast.config.AppConfig", "city.roast.handler", "city.roast.util", "city.roast.model"})
public class TestConfig {

}