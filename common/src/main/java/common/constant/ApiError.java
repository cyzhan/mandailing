package common.constant;

import lombok.Getter;

@Getter
public enum ApiError {


    CODE_401("unauthorized",401),
    CODE_404("data not found",404),
    CODE_429("too many request", 429),
    CODE_500("internal server error", 500),
    CODE_1000("too early for swap token", 1000),
    CODE_1001("unavailable", 1001),
    CODE_1002("incorrect account or password", 1002),
    CODE_4001("require lock fail", 4001);

    final private String msg;

    final private int code;

    ApiError(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

}
