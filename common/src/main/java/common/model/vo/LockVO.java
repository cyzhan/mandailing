package common.model.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LockVO {

    private Boolean obtained;

    private String key;

    private String valueForRls;

}
