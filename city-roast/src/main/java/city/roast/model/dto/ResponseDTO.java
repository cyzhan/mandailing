package city.roast.model.dto;

import city.roast.constant.GenericError;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO {

    private final int code;

    private final String msg;

    private final Object data;

    private static final ResponseDTO okResponse = new ResponseDTO(1, "ok");

    private static final ResponseDTO apiNotFoundResponse = new ResponseDTO(404, "api not found");

    private ResponseDTO(int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = null;
    }

    private ResponseDTO(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static ResponseDTO ok(){
        return okResponse;
    }

    public static ResponseDTO error(int code, String msg){
        return new ResponseDTO(code, msg);
    }

    public static ResponseDTO error(GenericError error){
        return new ResponseDTO(error.getCode(), error.getMsg());
    }

    public static ResponseDTO apiNotFound(){
        return apiNotFoundResponse;
    }

    public static ResponseDTO of(Object data){
        return new ResponseDTO(1, "ok", data);
    }

}
