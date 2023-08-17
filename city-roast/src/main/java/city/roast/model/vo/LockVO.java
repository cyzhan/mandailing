package city.roast.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class LockVO {

    private Boolean obtained;

    private String key;

    private String valueForRls;

}
