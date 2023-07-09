import java.util.LinkedHashMap;

public class DataStructure {

    public static void linkedHashmapDemo(){

        var linkedHashMap = new LinkedHashMap<Integer, String>(17, 75f, true);
        linkedHashMap.put(1, "jeff");
        linkedHashMap.put(2, "Rock");
        linkedHashMap.put(3, "Terry");
        linkedHashMap.put(4, "Andy");
        System.out.println(linkedHashMap);

        linkedHashMap.get(1);
        System.out.println(linkedHashMap);

        linkedHashMap.get(2);
        System.out.println(linkedHashMap);
    }

}
