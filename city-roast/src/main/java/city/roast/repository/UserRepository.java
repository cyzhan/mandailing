package city.roast.repository;

import city.roast.model.entity.User;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

//@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {

    @Query("SELECT * FROM city_roast.user WHERE user.name = ?")
    Mono<User> findByName(String name);


}
