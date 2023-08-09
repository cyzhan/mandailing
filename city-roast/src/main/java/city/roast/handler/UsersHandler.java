package city.roast.handler;

import city.roast.constant.Error;
import city.roast.constant.RedisKey;
import city.roast.exception.DomainLogicException;
import city.roast.model.vo.ResponseVO;
import city.roast.model.entity.User;
import city.roast.model.vo.ListWrapper;
import city.roast.model.vo.LoginVO;
import city.roast.model.vo.PageVO;
import city.roast.model.vo.UserVO;
import city.roast.repository.UserRepository;
import city.roast.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

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
    private AuthHelper authHelper;

    @Autowired
    private TransactionalOperator operator;

    @Autowired
    private RedisHelper redisHelper;

    @Autowired
    private ValidateHelper validateHelper;

    @Autowired
    private ExceptionHandler exceptionHandler;

    private final long tokenTTL;

    public UsersHandler(@Value("${app.token.ttl}") String tokenTTL) {
        this.tokenTTL = Long.parseLong(tokenTTL);
    }

    public Mono<ServerResponse> findByID(ServerRequest request){
        return Mono.just(request)
                .flatMap(req -> {
                    Long id = Long.parseLong(req.pathVariables().get("id"));
                    return userRepository.findByName("jeff");
                })
                .flatMap(userEntity -> ServerResponse.ok().bodyValue(userEntity));
    }

    public Mono<ServerResponse> login(ServerRequest request){
        final Map<String, Object> cache = new HashMap<>();
        final String LOGIN_VO = "login_vo";
        return request.bodyToMono(LoginVO.class)
                .doOnNext(validateHelper::validate)
                .doOnNext(loginVO -> cache.put(LOGIN_VO, loginVO))
                .flatMap(loginVO -> userRepository.findByName(loginVO.getName()))
                .switchIfEmpty(Mono.error(new DomainLogicException(Error.CODE_1001)))
                .map(user -> {
                    LoginVO loginVO = (LoginVO) cache.get(LOGIN_VO);
                    String encryptPW = EncryptHelper.md5(loginVO.getPassword());
                    if (encryptPW.equals(user.getPassword())){
                        String token = authHelper.generateToken(user);
                        return Tuples.of(RedisKey.loginUser(user.getId()), token);
                    }
                    throw new DomainLogicException(Error.CODE_1001);
                })
                .flatMap(tuple2 -> {
                    String token = tuple2.getT2();
                    String signature = token.split("\\.")[2];
                    return redisHelper.getTemplate().opsForValue()
                            .set(tuple2.getT1(), signature, Duration.ofSeconds(tokenTTL + 10L))
                            .zipWith(Mono.just(token), Tuples::of);
                })
                .flatMap(tuple2 -> {
                    if (!tuple2.getT1()) {
                        throw new DomainLogicException(Error.CODE_1001, "redis set loginUser fail");
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("token", tuple2.getT2());
                    return ServerResponse.ok().bodyValue(ResponseVO.of(map));
                })
                .onErrorResume(Exception.class, exceptionHandler::handle);
    }

    public Mono<ServerResponse> list(ServerRequest request){
        List<Long> longIDs = QueryParamHelper.asLongList(request, "ids");
        List<String> names = QueryParamHelper.asStringList(request, "name");
        PageVO pageVO = QueryParamHelper.parsePage(request);

        final List<Object> args = new ArrayList<>();
        StringBuilder whereBuilder = new StringBuilder();

        if (longIDs.size() > 0) {
            whereBuilder.append("WHERE u.id IN ")
                    .append(SqlHelper.createParentheses(longIDs.size()))
                    .append(" ");
            args.addAll(longIDs);
        } else if (names.size() > 0) {
            whereBuilder.append("WHERE u.name IN ")
                    .append(SqlHelper.createParentheses(names.size()));
            args.addAll(names);
        }

        args.add(pageVO.getOffset());
        args.add(pageVO.getSize());
        String mainSQL = "SELECT * FROM city_roast.user u "
                + whereBuilder
                + " LIMIT ?,?";
        String countSQL = "SELECT COUNT(u.id) AS count FROM city_roast.user u "
                + whereBuilder;

        log.debug(mainSQL);
        log.debug(countSQL);
        log.debug(args.toString());

        DatabaseClient.GenericExecuteSpec ges =  r2Template.getDatabaseClient().sql(mainSQL);
        for (int i = 0; i < args.size(); i++) {
            ges = ges.bind(i, args.get(i));
        }

        DatabaseClient.GenericExecuteSpec ges2 =  r2Template.getDatabaseClient().sql(countSQL);
        for (int i = 0; i < args.size()-2; i++) {
            ges2 = ges2.bind(i, args.get(i));
        }

        Mono<Long> longMono = ges2.map((row, rowMetaData) -> row.get("count", Long.class)).one();

        return ges.map((row, rowMetadata) -> User.builder()
                        .id(row.get("id", Long.class))
                        .name(row.get("name", String.class))
                        .email(row.get("email", String.class))
                        .password(row.get("password", String.class))
                        .balance(row.get("balance", BigDecimal.class))
                        .build())
                .all()
                .collectList()
                .zipWith(longMono, (users, count) -> {
                    return ResponseVO.of(ListWrapper.of(count, users));
                })
                .flatMap(data -> ServerResponse.ok().bodyValue(data));
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(UserVO.class)
                .doOnNext(validateHelper::validate)
                .flatMap(user -> {
                    String md5PW = EncryptHelper.md5(user.getPassword());
                    String sql = """
                            INSERT INTO city_roast.`user`(name, password, email, balance) VALUES 
                            (?,?,?,?)
                            """;

                    return r2Template.getDatabaseClient().sql(sql)
                            .bind(0, user.getName())
                            .bind(1, md5PW)
                            .bind(2, user.getEmail())
                            .bind(3, 0)
                            .fetch().rowsUpdated();
                }).flatMap(result -> {
                    log.info("rowsUpdated = {}", result);
                    return ServerResponse.ok().bodyValue(ResponseVO.ok());
                }).onErrorResume(e -> ServerResponse.ok().bodyValue(ResponseVO.error(500, e.getMessage())));
    }

    public Mono<ServerResponse> batch(ServerRequest request){
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
                    return ResponseVO.ok();
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
                            .thenReturn(ResponseVO.ok());
                })
                .flatMap(data -> ServerResponse.ok().bodyValue(data))
//                .as(operator::transactional)
                .onErrorResume(Exception.class, e -> {
                    return ServerResponse.status(500).bodyValue(ResponseVO.error(500, e.getMessage()));
                });
    }

}
