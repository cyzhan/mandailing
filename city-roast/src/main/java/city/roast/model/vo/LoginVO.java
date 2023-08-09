package city.roast.model.vo;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
public class LoginVO {

    @Length(min = 6, max = 16)
    @Pattern(regexp = "[a-z0-9]+$", message = "lowercase letters and numbers only")
    private String name;

    @Pattern(regexp = "^[a-zA-Z]\\w{5,17}$")
    private String password;

}
