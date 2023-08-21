package common.exception;

import common.constant.ApiError;
import lombok.Getter;

@Getter
public class DomainLogicException extends RuntimeException{

    private ApiError apiError;

    public DomainLogicException() {
    }

    public DomainLogicException(ApiError apiError){
        this.apiError = apiError;
    }

    public DomainLogicException(ApiError apiError, String msgPrintOnLog){
        super(msgPrintOnLog);
        this.apiError = apiError;
    }

}
