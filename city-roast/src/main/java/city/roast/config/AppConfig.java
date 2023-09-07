package city.roast.config;

import common.constant.RDBMS;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactory;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.log4j.Log4j2;
import org.mariadb.r2dbc.MariadbConnectionConfiguration;
import org.mariadb.r2dbc.MariadbConnectionFactory;
import org.mariadb.r2dbc.MariadbConnectionFactoryProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@ComponentScan(value = {"common.util"})
@Log4j2
public class AppConfig {

    @Bean
    @Primary
    public Validator getValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator();
        }
    }

    @Bean
    public CoffeeConnectionFactory getCoffeeConnectionFactory(Environment env){

        int connectionTimeoutSeconds = Integer.parseInt(Objects.requireNonNull(env.getProperty("rdbms.connection.timeout.seconds")));
        int primaryPoolInitialSize = Integer.parseInt(Objects.requireNonNull(env.getProperty("primary.rdbms.pool.initial.size")));
        int replicaPoolInitialSize = Integer.parseInt(Objects.requireNonNull(env.getProperty("replica.rdbms.pool.initial.size")));
        int primaryPoolMaxSize = Integer.parseInt(Objects.requireNonNull(env.getProperty("primary.rdbms.pool.max.size")));
        int replicaPoolMaxSize = Integer.parseInt(Objects.requireNonNull(env.getProperty("replica.rdbms.pool.max.size")));
        int poolMaxIdleMinutes = Integer.parseInt(Objects.requireNonNull(env.getProperty("rdbms.pool.max.idle.minutes")));
        int poolMaxAcquireSeconds = Integer.parseInt(Objects.requireNonNull(env.getProperty("rdbms.pool.max.acquire.seconds")));
        int poolMaxCreateConnectionSeconds = Integer.parseInt(Objects.requireNonNull(env.getProperty("rdbms.pool.max.create.connection.seconds")));
        int poolAcquireRetry = Integer.parseInt(Objects.requireNonNull(env.getProperty("rdbms.pool.acquire.retry")));

        MariadbConnectionConfiguration primaryConfig = MariadbConnectionConfiguration.builder()
                .host(Objects.requireNonNull(env.getProperty("primary.rdbms.host")))
                .database(Objects.requireNonNull(env.getProperty("primary.rdbms.database")))
                .username(Objects.requireNonNull(env.getProperty("primary.rdbms.username")))
                .password(Objects.requireNonNull(env.getProperty("primary.rdbms.password")))
                .port(Integer.parseInt(Objects.requireNonNull(env.getProperty("primary.rdbms.port"))))
                .tcpKeepAlive(Boolean.TRUE)
                .connectTimeout(Duration.of(connectionTimeoutSeconds, ChronoUnit.SECONDS))
                .build();
        MariadbConnectionFactory primaryConnectionFactory = MariadbConnectionFactory.from(primaryConfig);


        MariadbConnectionConfiguration replicaConfig = MariadbConnectionConfiguration.builder()
                .host(Objects.requireNonNull(env.getProperty("replica.rdbms.host")))
                .database(Objects.requireNonNull(env.getProperty("replica.rdbms.database")))
                .username(Objects.requireNonNull(env.getProperty("replica.rdbms.username")))
                .password(Objects.requireNonNull(env.getProperty("replica.rdbms.password")))
                .port(Integer.parseInt(Objects.requireNonNull(env.getProperty("replica.rdbms.port"))))
                .tcpKeepAlive(Boolean.TRUE)
                .connectTimeout(Duration.of(connectionTimeoutSeconds, ChronoUnit.SECONDS))
                .build();
        MariadbConnectionFactory replicaConnectionFactory = MariadbConnectionFactory.from(replicaConfig);


        ConnectionPoolConfiguration primaryConnectionPoolConfiguration = ConnectionPoolConfiguration.builder()
                .connectionFactory(primaryConnectionFactory)
                .initialSize(primaryPoolInitialSize)
                .maxSize(primaryPoolMaxSize)
                .maxIdleTime(Duration.ofMinutes(poolMaxIdleMinutes))
                .maxAcquireTime(Duration.ofSeconds(poolMaxAcquireSeconds))
                .acquireRetry(poolAcquireRetry)
                .maxCreateConnectionTime(Duration.ofSeconds(poolMaxCreateConnectionSeconds))
                .name(Objects.requireNonNull(env.getProperty("primary.rdbms.pool.name")))
                .validationQuery(Objects.requireNonNull(env.getProperty("rdbms.pool.validation.query")))
                .build();

        ConnectionPoolConfiguration replicaConnectionPoolConfiguration = ConnectionPoolConfiguration.builder()
                .connectionFactory(replicaConnectionFactory)
                .initialSize(replicaPoolInitialSize)
                .maxSize(replicaPoolMaxSize)
                .maxIdleTime(Duration.ofMinutes(poolMaxIdleMinutes))
                .maxAcquireTime(Duration.ofSeconds(poolMaxAcquireSeconds))
                .acquireRetry(poolAcquireRetry)
                .maxCreateConnectionTime(Duration.ofSeconds(poolMaxCreateConnectionSeconds))
                .name(Objects.requireNonNull(env.getProperty("replica.rdbms.pool.name")))
                .validationQuery(Objects.requireNonNull(env.getProperty("rdbms.pool.validation.query")))
                .build();

        Map<String, ConnectionFactory> factoryMap = new HashMap<>();
        factoryMap.put(RDBMS.PRIMARY, new ConnectionPool(primaryConnectionPoolConfiguration));
        factoryMap.put(RDBMS.REPLICA, new ConnectionPool(replicaConnectionPoolConfiguration));
        CoffeeConnectionFactory coffeeConnectionFactory = new CoffeeConnectionFactory();
        coffeeConnectionFactory.setTargetConnectionFactories(factoryMap);
        coffeeConnectionFactory.setDefaultTargetConnectionFactory(primaryConnectionFactory);
        return coffeeConnectionFactory;
    }

}
