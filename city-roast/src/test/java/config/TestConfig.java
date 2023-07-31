package config;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

//To avoid include config that contain annotations like @EnableScheduling, @EnableKafka
@SpringBootApplication
@EnableR2dbcRepositories(basePackages = {"city.roast.repository"})
@ComponentScan(value = {"city.roast.handler", "city.roast.util", "city.roast.model"})
@Log4j2
public class TestConfig {

}