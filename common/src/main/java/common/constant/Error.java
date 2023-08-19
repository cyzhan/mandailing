package common.constant;

import lombok.Getter;

@Getter
public enum Error {


    CODE_401("unauthorized",401),
    CODE_404("data not found",404),
    CODE_429("too many request", 429),
    CODE_1001("incorrect account or password", 1001),
    CODE_4001("require lock fail", 4001);

    final private String msg;

    final private int code;

    Error(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

}