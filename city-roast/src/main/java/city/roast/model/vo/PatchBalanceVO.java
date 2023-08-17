package city.roast.model.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PatchBalanceVO {

    private Long userId;

    private BigDecimal balance;


}
