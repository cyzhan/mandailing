import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class HackerRank {
    /**
     * Given an array of integers, calculate the ratios of its elements that are positive,
     * negative, and zero. Print the decimal value of each fraction on a new line with  places after the decimal.
     * <p>
     * Example [1,1,0,-1,-1]
     * <p>
     * 0.400000
     * 0.400000
     * 0.200000
     */
    public static void plusMinus(List<Integer> arr) {
        BigDecimal sum = new BigDecimal(arr.size());
        BigDecimal positive = BigDecimal.ZERO;
        BigDecimal equalZero = BigDecimal.ZERO;
        BigDecimal negative = BigDecimal.ZERO;
        for (Integer integer : arr) {
            BigDecimal bigDecimal = (new BigDecimal(integer)).setScale(6, RoundingMode.HALF_UP);
            if (bigDecimal.compareTo(BigDecimal.ZERO) > 0) {
                positive = positive.add(BigDecimal.ONE);
            } else if (bigDecimal.compareTo(BigDecimal.ZERO) == 0) {
                equalZero = equalZero.add(BigDecimal.ONE);
            } else {
                negative = negative.add(BigDecimal.ONE);
            }
        }
        System.out.println(positive.divide(sum, 6, RoundingMode.HALF_UP));
        System.out.println(negative.divide(sum, 6, RoundingMode.HALF_UP));
        System.out.println(equalZero.divide(sum, 6, RoundingMode.HALF_UP));
    }

    /**
     * Given five positive integers, find the minimum and maximum values that can be calculated by
     * summing exactly four of the five integers. Then print the respective minimum and maximum values
     * as a single line of two space-separated long integers.
     * <p>
     * Example [1,3,5,7,9]
     * <p>
     * 16  24
     */
    public static void miniMaxSum(List<Integer> arr) {
        // Write your code here
        int max = 0, min = 0;
        long maxSum = 0, minSum = 0;
        for (Integer integer : arr) {
            if (integer.compareTo(max) >= 0) {
                minSum += max;
                max = integer;
            } else {
                minSum += integer;
            }

            if (integer.compareTo(min) <= 0) {
                maxSum += min;
                min = integer;
            } else if (min == 0) {
                min = integer;
            } else {
                maxSum += integer;
            }
            System.out.println(maxSum);
        }
        System.out.printf("%d  %d", minSum, maxSum);
    }

    /**
     * Given a time in -hour AM/PM format, convert it to military (24-hour) time.
     * Note:
     * 12:00:00AM on a 12-hour clock is 00:00:00 on a 24-hour clock.
     * 12:00:00PM on a 12-hour clock is 12:00:00 on a 24-hour clock.
     * <p>
     * Example
     * 12:01:00PM return 12:01:00
     * 12:01:00AM return 00:01:00
     */
    public static String timeConversion(String s) {
        // Write your code here
        String numberPart = s.substring(0, 8);
        String stringPart = s.substring(8, 10);
        List<Integer> integers = Arrays.stream(numberPart.split(":")).map(Integer::valueOf).collect(Collectors.toList());

        switch (stringPart) {
            case "PM": {
                if (integers.get(0) != 12) {
                    integers.set(0, integers.get(0) + 12);
                }
            }
            break;
            case "AM": {
                if (integers.get(0) == 12) {
                    integers.set(0, 0);
                }
            }
            break;
            default:
                break;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (integers.get(i) < 10) {
                stringBuilder.append("0");
            }
            stringBuilder.append(integers.get(i));
            stringBuilder.append(":");
        }

        return stringBuilder.substring(0, 8);
    }

    /**
     * Given an array of integers, where all elements but one occur twice, find the unique element.
     * Example [1,2,3,4,3,2,1]
     * return 4
     */
    public static int lonelyinteger(List<Integer> a) {
        // Write your code here
        Map<Integer, Integer> map = new HashMap<>();
        a.forEach(integer -> {
            map.merge(integer, 1, Integer::sum);
        });
        Iterator<Map.Entry<Integer, Integer>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<Integer, Integer> entry = iterator.next();
            if (entry.getValue() == 1){
                return entry.getKey();
            }
        }
        return 0;
    }

    /**
     *  Given a square matrix, calculate the absolute difference between the sums of its diagonals.
     *
     *  Example:
     *  1 2 3
     *  4 5 6
     *  9 8 9
     *  |15 - 17| = 2
     */
    public static int diagonalDifference(List<List<Integer>> arr) {
        int matrixLength = arr.size();
        int diagonalLToR = 0, diagonalRToL = 0;
        for (int i = 0; i < matrixLength; i++) {
            diagonalLToR += arr.get(i).get(i);
            diagonalRToL += arr.get(i).get(matrixLength - 1 - i);
        }
        int difference = diagonalLToR - diagonalRToL;
        if (difference < 0){
            difference = difference * -1;
        }
        return difference;
    }

    /**
     * Example:
     * [1,1,3,2,1]
     * i	arr[i]	result
     * 0	1	[0, 1, 0, 0]
     * 1	1	[0, 2, 0, 0]
     * 2	3	[0, 2, 0, 1]
     * 3	2	[0, 2, 1, 1]
     * 4	1	[0, 3, 1, 1]
     */

    public static List<Integer> countingSort(List<Integer> arr) {
       int[] ary = new int[100];
        arr.forEach(integer -> {
            ary[integer] += 1;
        });
        return Arrays.stream(ary).boxed().collect(Collectors.toList());
    }

    public static List<Integer> reverseArray(List<Integer> arr) {
        // Write your code here
        int size = arr.size();
        List<Integer> reverseList = new ArrayList<>(size);
        for (int i = 0 ; i < size; i++) {
            reverseList.add(arr.get(size - 1 - i));
            System.out.println(arr.get(i));
        }
        return reverseList;
    }

//    public static List<Integer> reverseArray(List<Integer> arr) {
//        // Write your code here
//        int size = arr.size();
//
//        int[] ary = new int[size];
//        for (int i = 0 ; i < size; i++) {
//            ary[i] = arr.get(size - 1 - i);
//            System.out.println(arr.get(i));
//        }
//        return Arrays.stream(ary).boxed().toList();
//    }


//    public static long minStart(List<Integer> arr) {
//        int minSum = 1 - arr.get(0);
//        int runningSum = 1;
//        int x = Math.max(1, minSum);
//        System.out.println("init x = " + x);
//
//        for (int i = 1; i < arr.size(); i++) {
//            runningSum += arr.get(i);
//            minSum = Math.min(minSum, runningSum - 1);
//            x = Math.max(x, 1 - minSum);
//            System.out.println(" x = " + x);
//        }
//
//        return x;
//    }

    public static long minStart(List<Integer> arr) {
       long x, runningSum;
       if (arr.get(0) < 0){
            x = 1 - arr.get(0);
            runningSum = 1;
       }else {
           x = 1;
           runningSum = 1 + arr.get(0);
       }

        for (int i = 1; i < arr.size(); i++) {
            runningSum += arr.get(i);
            if (runningSum < 0){
                x = x - runningSum + 1;
                runningSum = 1;
            }
            System.out.println(String.format("value = %s,  x = %s , runningSum = %s", arr.get(i),x, runningSum));
        }

        return x;
    }
    //origin
    public static int findMinSwapsO(String brackets) {
        Stack<Character> stack = new Stack<>();
        int swaps = 0;

        for (char c : brackets.toCharArray()) {
            if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                if (!stack.isEmpty() && stack.peek() == '(') {
                    stack.pop();
                } else {
                    stack.push(c);
                }
            }
        }

        int openCount = 0;
        int closeCount = 0;
        while (!stack.isEmpty()) {
            char c = stack.pop();
            if (c == '(') {
                openCount++;
            } else if (c == ')') {
                closeCount++;
            }
        }

        if ((openCount + closeCount) % 2 != 0) {
            return -1;
        }

        int halfOpen = openCount / 2;
        int halfClose = closeCount / 2;
        swaps += halfOpen + halfClose;

        if (openCount % 2 != 0) {
            swaps += 1;
        }

        return swaps;
    }


    public static int findMinSwaps(String brackets) {
        Stack<Character> stack = new Stack<>();
        int swaps = 0;

        for (char c : brackets.toCharArray()) {
            if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                if (!stack.isEmpty() && stack.peek() == '(') {
                    stack.pop();
                } else {
                    stack.push(c);
                }
            }
        }

        int openCount = 0;
        int closeCount = 0;

        while (!stack.isEmpty()) {
            char c = stack.pop();
            if (c == '(') {
                openCount++;
            } else if (c == ')') {
                closeCount++;
            }
        }

//        if ((openCount + closeCount) % 2 != 0) {
//            return -1;
//        }

        if ((openCount != closeCount) || openCount % 2 != 0){
            return -1;
        }

        swaps = (openCount + closeCount) / 2;

        return swaps;
    }

    public static void treeMapDemo(){
        TreeMap<Integer, String> treeMap = new TreeMap<>();
        treeMap.put(1, "Jeff");
        treeMap.put(17, "Bob");
        treeMap.put(7, "Ken");
        treeMap.put(34, "Ben");

        Map.Entry<Integer, String> entry = treeMap.firstEntry();

        entry = treeMap.lastEntry();

        Set<Map.Entry<Integer, String>> entrySet = treeMap.headMap(3).entrySet();

        entrySet = treeMap.tailMap(3).entrySet();

        entrySet = treeMap.subMap(2,4).entrySet();
    }

    public static void Demo(){
        TreeMap<Integer, String> treeMap = new TreeMap<>();
        treeMap.put(1, "Jeff");
        treeMap.put(17, "Bob");
        treeMap.put(7, "Ken");
        treeMap.put(34, "Ben");

        Map.Entry<Integer, String> entry = treeMap.firstEntry();

        entry = treeMap.lastEntry();

        Set<Map.Entry<Integer, String>> entrySet = treeMap.headMap(3).entrySet();

        entrySet = treeMap.tailMap(3).entrySet();

        entrySet = treeMap.subMap(2,4).entrySet();
    }

}

