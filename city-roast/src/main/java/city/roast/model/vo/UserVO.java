package city.roast.model.vo;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserVO {

//    @Length(min = 6, max = 16)
    private String name;

    @Length(min = 8, max = 32)
    private String password;

    @Email
    private String email;

}
