package city.roast.handler;

import city.roast.model.dto.ResponseDTO;
import city.roast.model.entity.User;
import city.roast.model.vo.PageVO;
import city.roast.repository.UserRepository;
import city.roast.util.QueryParamHelper;
import city.roast.util.SqlHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.spi.Result;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;

import static city.roast.util.SqlHelper.CURRENT_TIMESTAMP;

@Service
@Log4j2
public class UsersHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private R2dbcEntityTemplate r2Template;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionalOperator operator;

    public Mono<ServerResponse> findByID(ServerRequest request){
        return Mono.just(request)
                .flatMap(req -> {
                    Long id = Long.parseLong(req.pathVariables().get("id"));
                    return userRepository.findByName("jeff");
                })
                .flatMap(userEntity -> ServerResponse.ok().bodyValue(userEntity));
    }

    public Mono<ServerResponse> findByCondition(ServerRequest request){
        return Mono.just(request)
                .flatMap(req -> {
                    Optional<String> optional = req.queryParam("name");
                    if (optional.isPresent()){
                        return userRepository.findByName(optional.get());
                    }else{
                        return Mono.empty();
                    }
                })
                .flatMap(userEntity -> ServerResponse.ok().bodyValue(userEntity));
    }

    public Mono<ServerResponse> list(ServerRequest request){
        List<Long> longIDs = QueryParamHelper.asLongList(request, "ids");
        List<String> names = QueryParamHelper.asStringList(request, "name");
        PageVO pageVO = QueryParamHelper.parsePage(request);

        final List<Object> args = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * FROM city_roast.user u ");
        if (longIDs.size() > 0) {
            builder.append("WHERE u.id IN ")
                    .append(SqlHelper.createParentheses(longIDs.size()))
                    .append(" ");
            args.addAll(longIDs);
        } else if (names.size() > 0) {
            builder.append("WHERE u.name IN ")
                    .append(SqlHelper.createParentheses(names.size()))
                    .append(" ");
            args.addAll(names);
        }
        builder.append("LIMIT ?,?");
        args.add(pageVO.getOffset());
        args.add(pageVO.getSize());
        String sql = builder.toString();
        log.debug(sql);
        log.debug(args.toString());

        DatabaseClient.GenericExecuteSpec ges =  r2Template.getDatabaseClient().sql(sql);
        for (int i = 0; i < args.size(); i++) {
            ges = ges.bind(i, args.get(i));
        }
        return ges.map((row, rowMetadata) -> {
                    return User.builder().id(row.get("id", Long.class))
                            .name(row.get("name", String.class))
                            .email(row.get("email", String.class))
                            .password(row.get("password", String.class))
                            .balance(row.get("balance", String.class)).build();
                })
                .all()
                .collectList()
                .map(ResponseDTO::of)
                .flatMap(data -> ServerResponse.ok().bodyValue(data));
    }

    public Mono<ServerResponse> batch(ServerRequest request){
//        r2Template.getDatabaseClient().sql("").fetch().rowsUpdated()
        return request.bodyToFlux(User.class).collectList()
                .flatMap(list -> {
                    final StringBuilder builder = new StringBuilder();
                    final List<Object> args = new ArrayList<>(100);
                    String sql =
                            "INSERT INTO city_roast.`user`(name, password, email, balance) VALUES";
                    builder.append(sql).append(" ");

                    list.forEach(user -> {
                        builder.append(SqlHelper.createParentheses(4)).append(",");
                        args.addAll(List.of(user.getName(), user.getPassword(), user.getEmail(), 0));
                    });

                    builder.deleteCharAt(builder.length() - 1);
                    sql = builder.toString();
                    log.debug(sql);
                    DatabaseClient.GenericExecuteSpec ges =  r2Template.getDatabaseClient().sql(sql);
                    for (int i = 0; i < args.size(); i++) {
                        ges = ges.bind(i, args.get(i));
                    }
                    return ges.fetch().rowsUpdated();
                })
                .map(updatedRow -> {
                    log.info("updatedRow = {}", updatedRow);
                    return ResponseDTO.ok();
                })
                .flatMap(data -> ServerResponse.ok().bodyValue(data));
    }

    //one row will be inserted with transactional annotation command out
    //with onErrorResume, rollback will not work.
//    @Transactional
    public Mono<ServerResponse> txTest(ServerRequest request){
        return request.bodyToFlux(User.class).collectList()
                .flatMap(list -> {
                    String sql = """
                            INSERT INTO city_roast.`user`(name, password, email, balance) VALUES 
                            (?,?,?,?)
                            """;
                    User user = list.get(0);
                    return r2Template.getDatabaseClient().sql(sql)
                            .bind(0, user.getName())
                            .bind(1, user.getPassword())
                            .bind(2, user.getEmail())
                            .bind(3, 0)
                            .then()
                            .thenReturn(list);

                })
                .flatMap(list -> {
                    log.info("list.get(0) completed");
                    String sql = """
                            INSERT INTO city_roast.`user`(name, password, email, balance) VALUES 
                            (?,?,?,?)
                            """;
                    User user = list.get(1);
                    return r2Template.getDatabaseClient().sql(sql)
                            .bind(0, user.getName())
                            .bind(1, user.getPassword())
                            .bind(2, user.getEmail())
//                            .bind(3, 0)
                            //command out above code to deliberately invoke exception
                            .then()
                            .thenReturn(ResponseDTO.ok());
                })
                .flatMap(data -> ServerResponse.ok().bodyValue(data))
//                .as(operator::transactional)
                .onErrorResume(Exception.class, e -> {
                    return ServerResponse.status(500).bodyValue(ResponseDTO.error(500, e.getMessage()));
                });
    }

}
