import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class MainTest {

    @Test
    public void run() {
//        var result = LeeCode.combinationSum(new int[]{2, 3, 6, 7}, 7);
//        System.out.println("result = " + result);

//        var result = LeeCode.combinationSum2(new int[]{10,1,2,7,6,1,5}, 8);
//        System.out.println("result = " + result);

//        var result = LeeCode.jump(new int[]{2,3,1,1,4});
//        System.out.println("result = " + result);

        //should be 2
        var result2 = LeeCode.jump(new int[]{2,0,2,0,1});
        System.out.println("result = " + result2);

    }
}
