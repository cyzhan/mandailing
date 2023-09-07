package city.roast.handler;

import city.roast.model.vo.*;
import common.constant.ApiError;
import city.roast.model.entity.User;
import city.roast.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.constant.ContextKey;
import common.constant.RDBMS;
import common.constant.RedisKey;
import common.exception.DomainLogicException;
import common.model.vo.ListWrapper;
import common.model.vo.PageVO;
import common.model.vo.ObjectWrapper;
import common.util.*;
import common.util.AuthHelper;
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
                .flatMap(req -> userRepository.findById(Long.parseLong(req.pathVariables().get("id"))))
                .switchIfEmpty(Mono.error(DomainLogicException.of(ApiError.CODE_404)))
                .flatMap(userEntity -> ServerResponse.ok().bodyValue(ObjectWrapper.of(userEntity)))
                .onErrorResume(exceptionHandler::handle);
    }

    public Mono<ServerResponse> login(ServerRequest request){
        return request.bodyToMono(LoginVO.class)
                .doOnNext(validateHelper::validate)
                .flatMap(loginVO -> Mono.just(loginVO).zipWith(
                        userRepository.findByName(loginVO.getName())
                                .switchIfEmpty(Mono.error(DomainLogicException.of(ApiError.CODE_1001)))
                                .contextWrite(context -> context.put(ContextKey.DATA_SOURCE, RDBMS.REPLICA)),
                        Tuples::of)
                )
                .map(tuples -> {
                    LoginVO loginVO = tuples.getT1();
                    User user = tuples.getT2();
                    String encryptPW = EncryptHelper.md5(loginVO.getPassword());
                    if (encryptPW.equals(user.getPassword())){
                        String token = authHelper.generateToken(user.getId(), user.getName());
                        return Tuples.of(RedisKey.loginUser(user.getId()), token);
                    }
                    throw DomainLogicException.of(ApiError.CODE_1002);
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
                        return Mono.error(DomainLogicException.of(ApiError.CODE_1002, "redis set loginUser fail"));
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("token", tuple2.getT2());
                    return ServerResponse.ok().bodyValue(ObjectWrapper.of(map));
                })
                .onErrorResume(exceptionHandler::handle);
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

        Mono<Long> longMono = ges2.map((row, rowMetaData) -> row.get("count", Long.class))
                .one()
                .contextWrite(context -> context.put(ContextKey.DATA_SOURCE, RDBMS.REPLICA));

        return ges.map((row, rowMetadata) -> User.builder()
                        .id(row.get("id", Long.class))
                        .name(row.get("name", String.class))
                        .email(row.get("email", String.class))
                        .password(row.get("password", String.class))
                        .balance(row.get("balance", BigDecimal.class))
                        .build())
                .all()
                .collectList()
                .contextWrite(context -> context.put(ContextKey.DATA_SOURCE, RDBMS.REPLICA))
                .zipWith(longMono, (users, count) -> ObjectWrapper.of(ListWrapper.of(count, users)))
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
                })
                .contextWrite(context -> context.put(ContextKey.DATA_SOURCE, RDBMS.PRIMARY))
                .doOnNext(rowsUpdated -> log.info("rowsUpdated = {}", rowsUpdated))
                .flatMap(result -> ServerResponse.ok().bodyValue(ObjectWrapper.ok()))
                .onErrorResume(exceptionHandler::handle);
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
                    return ObjectWrapper.ok();
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
                            .thenReturn(ObjectWrapper.ok());
                })
                .flatMap(data -> ServerResponse.ok().bodyValue(data))
                .as(operator::transactional)
                .contextWrite(context -> context.put(ContextKey.DATA_SOURCE, RDBMS.PRIMARY))
                .onErrorResume(exceptionHandler::handle);
    }

    public Mono<ServerResponse> patchBalance(ServerRequest request){
        return request.bodyToMono(PatchBalanceVO.class)
                .doOnNext(validateHelper::validate)
                .flatMap(patchBalanceVO -> {
                    return redisHelper.obtainLock(RedisKey.balanceLock(patchBalanceVO.getUserId()), UUID.randomUUID().toString())
                            .zipWith(Mono.just(patchBalanceVO), Tuples::of);
                })
                .doOnNext(tuple2 -> {
                    if (!tuple2.getT1().getObtained()){
                        throw DomainLogicException.of(ApiError.CODE_4001);
                    }
                })
                .flatMap(tuple2 -> {
                    String sql = """
                            UPDATE city_roast.`user` u SET balance = ? WHERE u.id = ?;
                            """;

                    return r2Template.getDatabaseClient().sql(sql)
                            .bind(0, tuple2.getT2().getBalance())
                            .bind(1, tuple2.getT2().getUserId())
                            .fetch()
                            .rowsUpdated()
                            .zipWith(Mono.just(tuple2.getT1()), Tuples::of);
                })
                .contextWrite(context -> context.put(ContextKey.DATA_SOURCE, RDBMS.PRIMARY))
                .flatMap(tuple2 -> {
                    String key = tuple2.getT2().getKey();
                    String valueForRls = tuple2.getT2().getValueForRls();
                    return redisHelper.rlsLock(key, valueForRls);
                })
                .flatMap(rlsLockResult -> {
                    if (rlsLockResult != 1){
                        log.info("rls redis fail");
                    }
                    return ServerResponse.ok().bodyValue(ObjectWrapper.ok());
                })
                .onErrorResume(exceptionHandler::handle);
    }

}
