package city.roast.constant;

public enum GenericError {


    CODE_401("unauthorized", "",401),
    CODE_429("too many request", "", 429);

    final private String msg;

    final private String reason;

    final private int code;

    GenericError(String msg, String reason, int code) {
        this.msg = msg;
        this.code = code;
        this.reason = reason;
    }

    public String getMsg() {
        return msg;
    }

    public int getCode() {
        return code;
    }

    public String getReason() {
        return reason;
    }

}
