import java.util.*;

public class LeeCode {
    /**
     * 36 Valid Sudoku
     * Each row must contain the digits 1-9 without repetition.
     * Each column must contain the digits 1-9 without repetition.
     * Each of the nine 3 x 3 sub-boxes of the grid must contain the digits 1-9 without repetition.
     * A Sudoku board (partially filled) could be valid but is not necessarily solvable.
     */
    public static boolean problem36(char[][] board){
        Map<Integer, Integer> rowElementsCount = new HashMap<>();
        Map<Integer, Integer>[] columnElementsCount = new HashMap[9];
        Map<Integer, Integer>[][] subBoxesElementsCount = new HashMap[3][3];

        for (int i = 0; i < 9; i++) {
            columnElementsCount[i] = new HashMap<>();
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                subBoxesElementsCount[i][j] = new HashMap<>();
            }
        }

        for (int i = 0; i < 9; i++) {
            rowElementsCount.clear();
            char[] row = board[i];
            for (int j = 0; j < 9; j++) {
//                System.out.printf("i = %d, j = %d\n", i, j);
                if (row[j] == '.'){
                    continue;
                }
                Integer value = (int) row[j];
                if (rowElementsCount.get(value) == null){
                    rowElementsCount.put(value, 1);
                }else{
                    return false;
                }

                value = (int) board[i][j];
                if (columnElementsCount[j].get(value) == null){
                    columnElementsCount[j].put(value, 1);
                }else{
                    return false;
                }

                int indexX = i / 3;
                int indexY = j / 3;
                if (subBoxesElementsCount[indexX][indexY].get(value) == null){
                    subBoxesElementsCount[indexX][indexY].put(value, 1);
                }else{
                    return false;
                }
            }
        }

        return true;
    }

    public static String problem38(int n){
        if (n == 1){
            return "1";
        }

        StringBuilder builder = new StringBuilder();
        char[] charArray = problem38(n-1).toCharArray();
        int count = 0;
        int length = charArray.length;
        if (length == 1){
            return "11";
        }

        for (int i = 0; i < length; i++) {

            if (i == 0){
                count = 1;
                continue;
            }

            if ((i == length -1) && (charArray[i] == charArray[i - 1])){
                count += 1;
                builder.append(count);
                builder.append(charArray[i]);
                break;
            }else if (i == length -1){
                builder.append(count);
                builder.append(charArray[i -1]);
                builder.append("1");
                builder.append(charArray[i]);
                break;
            }

            if(charArray[i] == charArray[i - 1]){
                count += 1;
            }else{
                builder.append(count);
                builder.append(charArray[i-1]);
                count = 1;
            }
        }

       return builder.toString();
    }

    /**
     * problem 39
     */
    public static List<List<Integer>> combinationSum(int[] candidates, int target){
        Arrays.sort(candidates);
        List<List<Integer>> result2DArray = new ArrayList<>();
        int length = candidates.length;
        for (int i = 0; i < length; i++) {

            int candidate = candidates[i];
            int[] newCandidates = new int[length - i];
            for (int j = i; j < length; j++) {
                newCandidates[j-i] = candidates[j];
            }

            int newTarget = target - candidate;
//            System.out.printf("candidate = %s, target = %s\n", candidate, target);
            if (newTarget > 0) {
//                System.out.printf("recursive call, newCandidates = %s, newTarget = %s\n", Arrays.toString(newCandidates), newTarget);
                List<List<Integer>> subResult2DArray = combinationSum(newCandidates, newTarget);
                for (List<Integer> list : subResult2DArray) {
                    list.add(candidate);
                    result2DArray.add(list);
                }
            } else if (newTarget == 0) {
                List<Integer> list = new ArrayList<>(1);
                list.add(candidate);
                result2DArray.add(list);
                break;
            } else {
                break;
            }
        }

        return result2DArray;
    }

    /**
     * problem 40
     */
    public static List<List<Integer>> combinationSum2(int[] candidates, int target) {
        Arrays.sort(candidates);
        int length = candidates.length;
        List<List<Integer>> result2DArray = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            if (i > 0 && candidates[i] == candidates[i-1]){
                continue;
            }

            int candidate = candidates[i];
            int newTarget =  target - candidate;
//            System.out.printf("candidate = %s, target = %s\n", candidate, target);
            int[] newCandidates = new int[length - i - 1];
            for (int j = i + 1; j < length; j++) {
                newCandidates[j-i-1] = candidates[j];
            }

            if (newTarget > 0){
//                System.out.printf("recursive call, newCandidates = %s, newTarget = %s\n", Arrays.toString(newCandidates), newTarget);
                List<List<Integer>> recursiveResult = combinationSum2(newCandidates, newTarget);
                for (List<Integer> list : recursiveResult) {
                    list.add(candidate);
                    result2DArray.add(list);
                }
            }else if (newTarget == 0){
                List<Integer> list = new ArrayList<>(1);
                list.add(candidate);
                result2DArray.add(list);
                break;
            }else{
                break;
            }
        }
        return result2DArray;
    }

    //Time Limit Exceeded
    public static int jump(int[] nums) {
        final int length = nums.length;
        if (length == 1){
            return 0;
        }
        int maxSteps = nums[0];
        if (length <= maxSteps + 1){
            return 1;
        }

        int minRecursiveResult = 10000;
        int[] newNumbers;
        for (int i = 1; i <= maxSteps; i++) {
            if(nums[i] == 0){
                continue;
            }

            newNumbers = Arrays.copyOfRange(nums, i, length);
            System.out.printf("newNumbers = %s\n", Arrays.toString(newNumbers));
            int recursiveResult = jump(newNumbers);
            System.out.printf("recursiveResult = %s\n", recursiveResult);

            if (recursiveResult < minRecursiveResult) {
                minRecursiveResult = recursiveResult;
            }
        }
        System.out.printf("minRecursiveResult = %s\n", minRecursiveResult);
        return minRecursiveResult + 1;
    }

}
