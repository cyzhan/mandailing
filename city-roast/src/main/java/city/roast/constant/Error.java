package city.roast.constant;

import lombok.Getter;

@Getter
public enum Error {


    CODE_401("unauthorized",401),
    CODE_429("too many request", 429),
    CODE_1001("incorrect account or password", 1001);

    final private String msg;

    final private int code;

    Error(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

}
