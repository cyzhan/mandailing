package common.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import common.constant.ApiError;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObjectWrapper {

    private final int code;

    private final String msg;

    private final Object data;

    private static final String OK = "ok";

    private static final ObjectWrapper okWrapper= new ObjectWrapper(1, OK);

    private static final ObjectWrapper apiNotFoundWrapper = new ObjectWrapper(404, "api not found");

    private ObjectWrapper(int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = null;
    }

    private ObjectWrapper(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static ObjectWrapper ok(){
        return okWrapper;
    }

    public static ObjectWrapper error(int code, String msg){
        return new ObjectWrapper(code, msg);
    }

    public static ObjectWrapper error(ApiError error){
        return new ObjectWrapper(error.getCode(), error.getMsg());
    }

    public static ObjectWrapper apiNotFound(){
        return apiNotFoundWrapper;
    }

    public static ObjectWrapper of(Object data){
        return new ObjectWrapper(1, OK, data);
    }

}
