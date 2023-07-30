package city.roast.util;


import io.lettuce.core.Value;
import io.lettuce.core.dynamic.Commands;
import io.lettuce.core.dynamic.annotation.Command;

import java.util.List;

public interface RedisCommand extends Commands {

    @Command("MGET")
    List<Value<String>> mgetAsValues(String... keys);

}
