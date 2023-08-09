package city.roast.exception;

import city.roast.constant.Error;
import lombok.Getter;

@Getter
public class DomainLogicException extends RuntimeException{

    private Error error;

    public DomainLogicException() {
    }

    public DomainLogicException(Error error){
        this.error = error;
    }

    public DomainLogicException(Error error, String message){
        super(message);
        this.error = error;
    }

}
