package simple;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HackerRank {
    /**
     *  Given an array of integers, calculate the ratios of its elements that are positive,
     *  negative, and zero. Print the decimal value of each fraction on a new line with  places after the decimal.
     *
     *  Example [1,1,0,-1,-1]
     *
     *  0.400000
     *  0.400000
     *  0.200000
     */
    public static void plusMinus(List<Integer> arr) {
        BigDecimal sum = new BigDecimal(arr.size());
        BigDecimal positive = BigDecimal.ZERO;
        BigDecimal equalZero = BigDecimal.ZERO;
        BigDecimal negative = BigDecimal.ZERO;
        for (Integer integer : arr) {
            BigDecimal bigDecimal = (new BigDecimal(integer)).setScale(6, RoundingMode.HALF_UP);
            if (bigDecimal.compareTo(BigDecimal.ZERO) > 0){
                positive = positive.add(BigDecimal.ONE);
            }
            else if (bigDecimal.compareTo(BigDecimal.ZERO) == 0){
                equalZero = equalZero.add(BigDecimal.ONE);
            } else{
                negative = negative.add(BigDecimal.ONE);
            }
        }
        System.out.println(positive.divide(sum, 6, RoundingMode.HALF_UP));
        System.out.println(negative.divide(sum, 6, RoundingMode.HALF_UP));
        System.out.println(equalZero.divide(sum, 6, RoundingMode.HALF_UP));
    }

    /**
     *  Given five positive integers, find the minimum and maximum values that can be calculated by
     *  summing exactly four of the five integers. Then print the respective minimum and maximum values
     *  as a single line of two space-separated long integers.
     *
     *  Example [1,3,5,7,9]
     *
     *  16  24
     */
    public static void miniMaxSum(List<Integer> arr) {
        // Write your code here
        int max = 0, min = 0;
        long maxSum = 0, minSum = 0;
        for (Integer integer : arr) {
            if (integer.compareTo(max) >= 0){
                minSum += max;
                max = integer;
            }else{
                minSum += integer;
            }

            if (integer.compareTo(min) <= 0){
                maxSum += min;
                min = integer;
            }else if(min == 0) {
                min = integer;
            }else{
                maxSum += integer;
            }
            System.out.println(maxSum);
        }
        System.out.printf("%d  %d", minSum, maxSum);
    }

    /**
     *  Given a time in -hour AM/PM format, convert it to military (24-hour) time.
     *  Note:
     *  12:00:00AM on a 12-hour clock is 00:00:00 on a 24-hour clock.
     *  12:00:00PM on a 12-hour clock is 12:00:00 on a 24-hour clock.
     *
     *  Example
     *  12:01:00PM return 12:01:00
     *  12:01:00AM return 00:01:00
     */
    public static String timeConversion(String s) {
        // Write your code here
        String numberPart = s.substring(0,8);
        String stringPart = s.substring(8,10);
        List<Integer> integers = Arrays.stream(numberPart.split(":")).map(Integer::valueOf).collect(Collectors.toList());

        switch (stringPart){
            case "PM": {
                if (integers.get(0) != 12) {
                    integers.set(0, integers.get(0) + 12);
                }
            }break;
            case "AM": {
                if (integers.get(0) == 12) {
                    integers.set(0, 0);
                }
            }break;
            default:
                break;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (integers.get(i) < 10){
                stringBuilder.append("0");
            }
            stringBuilder.append(integers.get(i));
            stringBuilder.append(":");
        }

        return stringBuilder.substring(0, 8);
    }

}
