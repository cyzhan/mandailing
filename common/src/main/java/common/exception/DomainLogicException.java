package common.exception;

import common.constant.ApiError;
import lombok.Getter;

@Getter
public class DomainLogicException extends RuntimeException{

    private ApiError apiError;

    public DomainLogicException() {
    }

    public static DomainLogicException of(ApiError apiError){
        return new DomainLogicException(apiError);
    }

    public static DomainLogicException of(ApiError apiError, String logMsg){
        return new DomainLogicException(apiError, logMsg);
    }

    private DomainLogicException(ApiError apiError){
        this.apiError = apiError;
    }

    private DomainLogicException(ApiError apiError, String msgPrintOnLog){
        super(msgPrintOnLog);
        this.apiError = apiError;
    }

}
