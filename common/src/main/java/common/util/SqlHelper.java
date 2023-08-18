package common.util;

public class SqlHelper {

    public static final String CURRENT_TIMESTAMP = "current_timestamp(3)";

    /**
     * @param number represent the number of '?'
     * @return "(?,?,?)"
     */
    public static String createParentheses(int number){
        StringBuilder builder = new StringBuilder("(");
        builder.append("?,".repeat(Math.max(0, number)));
        builder.deleteCharAt(builder.length() - 1);
        builder.append(")");
        return builder.toString();
    }

}
