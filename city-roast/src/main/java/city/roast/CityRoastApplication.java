package city.roast;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages="city.roast")
public class CityRoastApplication {

    public static void main(String[] args) {
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        SpringApplication springApplication = new SpringApplication(CityRoastApplication.class);
        springApplication.run(args);
    }

}
