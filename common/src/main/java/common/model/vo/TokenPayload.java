package common.model.vo;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import common.annotation.TokenField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class TokenPayload {

    @TokenField
    private long userId;

    @TokenField
    private String name;

}
