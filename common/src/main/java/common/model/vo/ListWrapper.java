package common.model.vo;




import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListWrapper<T> {

    private final Long count;

    private final List<T> items;

    private static final List<Object> emptyList = Collections.unmodifiableList(new ArrayList<>(0));

    private ListWrapper(Long count, List<T> items) {
        this.count = count;
        this.items = items;
    }


    public static <T>ListWrapper<T> of(Long count, List<T> items){
        return new ListWrapper<>(count, items);
    }

    public static ListWrapper<Object> empty(){
        return new ListWrapper<>(0L, emptyList);
    }

}
