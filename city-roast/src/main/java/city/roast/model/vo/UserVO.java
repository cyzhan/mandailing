package city.roast.model.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserVO {

    @Length(min = 6, max = 16)
    @Pattern(regexp = "[a-z0-9]+$", message = "lowercase letters and numbers only")
    private String name;

//    @Length(min = 8, max = 32)
    @Pattern(regexp = "^[a-zA-Z]\\w{5,17}$")
    private String password;

    @Email
    private String email;

}
