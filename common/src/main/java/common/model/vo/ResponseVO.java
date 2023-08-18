package common.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import common.constant.Error;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseVO {

    private final int code;

    private final String msg;

    private final Object data;

    private static final ResponseVO okResponse = new ResponseVO(1, "ok");

    private static final ResponseVO apiNotFoundResponse = new ResponseVO(404, "api not found");

    private ResponseVO(int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = null;
    }

    private ResponseVO(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static ResponseVO ok(){
        return okResponse;
    }

    public static ResponseVO error(int code, String msg){
        return new ResponseVO(code, msg);
    }

    public static ResponseVO error(Error error){
        return new ResponseVO(error.getCode(), error.getMsg());
    }

    public static ResponseVO apiNotFound(){
        return apiNotFoundResponse;
    }

    public static ResponseVO of(Object data){
        return new ResponseVO(1, "ok", data);
    }

}
