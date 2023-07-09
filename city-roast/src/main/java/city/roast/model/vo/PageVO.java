package city.roast.model.vo;

import lombok.Getter;

@Getter
public class PageVO {

    private final int offset;

    private final int size;

    public PageVO(int offset, int size) {
        this.offset = offset;
        this.size = size;
    }

    public static PageVO of(int page, int size){
        if (size < 10) {
            size = 10;
        }else if(size > 200){
            size = 200;
        }

        return new PageVO((page - 1)*size, size);
    }

    @Override
    public String toString() {
        return "PageVO{" +
                "offset=" + offset +
                ", size=" + size +
                '}';
    }

}
