package city.roast.util;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Log4j2
public class ValidateHelper {

    private final Validator validator;

    public ValidateHelper(@Autowired Validator validator) {
        this.validator = validator;
    }

    public <T> void validate(T t){
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(t);
        for (ConstraintViolation<T> violation : constraintViolations) {
            String errorMsg = String.format("%s: %s", violation.getPropertyPath().toString(), violation.getMessage());
            log.error(errorMsg);
            throw new ValidationException(errorMsg);
        }
    }

}