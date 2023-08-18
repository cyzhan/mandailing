package common.util;


import common.model.vo.PageVO;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryParamHelper {

    private static final List<Long> emptyLongList = Collections.unmodifiableList(new ArrayList<>(0));

    private static final List<Integer> emptyIntList = Collections.unmodifiableList(new ArrayList<>(0));

    private static final List<String> emptyStringList = Collections.unmodifiableList(new ArrayList<>(0));


    public static List<Long> asLongList(ServerRequest request, String name){
        List<String> strings = request.queryParams().get(name);
        return strings != null ? strings.stream().map(Long::parseLong).toList() : emptyLongList;
    }

    public static List<Integer> asIntList(ServerRequest request, String name){
        List<String> strings = request.queryParams().get(name);
        return strings != null ? strings.stream().map(Integer::parseInt).toList() : emptyIntList;
    }

    public static List<String> asStringList(ServerRequest request, String name){
        return request.queryParams().get("name") != null ? request.queryParams().get("name") : emptyStringList;
    }

    public static PageVO parsePage(ServerRequest request){
        int size = request.queryParam("size").map(Integer::valueOf).orElse(10);
//        int offset = request.queryParam("page").map(page -> (Integer.parseInt(page) - 1) * size).orElse(0);
        int page = request.queryParam("page").map(Integer::parseInt).orElse(1);
        return PageVO.of(page, size);
    }

}
