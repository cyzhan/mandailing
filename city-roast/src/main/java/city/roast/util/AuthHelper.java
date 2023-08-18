package city.roast.util;

import city.roast.annotation.TokenField;
import city.roast.model.TokenPayload;
import city.roast.model.entity.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@Log4j2
public class AuthHelper {

    private final Algorithm algorithm;

    private final long tokenTTL;

    public AuthHelper(@Value("${app.auth.secret}") String secret, @Value("${app.token.ttl}") String tokenTTL) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.tokenTTL = Long.parseLong(tokenTTL);
    }

    public String generateToken(User user) {
        TokenPayload tokenPayload = new TokenPayload();
        tokenPayload.setUserId(user.getId());
        tokenPayload.setName(user.getName());
        return generateToken(tokenPayload);
    }

    public String generateToken(TokenPayload tokenPayload) {
        Instant instant = Instant.now();
        JWTCreator.Builder builder = JWT.create();
        builder.withIssuedAt(instant)
                .withExpiresAt(instant.plus(tokenTTL, ChronoUnit.SECONDS));
        Field[] fields = TokenPayload.class.getDeclaredFields();
        Class<?> c = null;
        for (Field field : fields) {
            if (field.isAnnotationPresent(TokenField.class)) {
                field.setAccessible(true);
                c = field.getType();
                try {
                    if (c == String.class) {
                        builder.withClaim(field.getName(), (String) field.get(tokenPayload));
                    } else if (c == Integer.class || c == int.class) {
                        builder.withClaim(field.getName(), (Integer) field.get(tokenPayload));
                    } else if (c == Boolean.class) {
                        builder.withClaim(field.getName(), (Boolean) field.get(tokenPayload));
                    } else if (c == Long.class || c == long.class) {
                        builder.withClaim(field.getName(), (Long) field.get(tokenPayload));
                    } else if (c == Double.class) {
                        builder.withClaim(field.getName(), (Double) field.get(tokenPayload));
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return builder.sign(algorithm);
    }

}
