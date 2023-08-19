package common.exception;

import common.constant.Error;
import lombok.Getter;

@Getter
public class DomainLogicException extends RuntimeException{

    private Error error;

    public DomainLogicException() {
    }

    public DomainLogicException(Error error){
        this.error = error;
    }

    public DomainLogicException(Error error, String msgPrintOnLog){
        super(msgPrintOnLog);
        this.error = error;
    }

}
