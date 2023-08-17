package city.roast.constant;

public class RedisKey {

    public static String token(int userId, long produceTime){
        return String.format("userId:%s,produceTime:%s", userId, produceTime);
    }

    public static String loginUser(long userId){
        return String.format("loginUser:%s", userId);
    }


    public static String balanceLock(long userId){
        return "userBalance:" + userId;
    }

    public static String loginFailCount(String username){
        return String.format("loginCount:%s", username);
    }

    public static String requestCount(int userId){
        return String.format("requestCount:%s", userId);
    }

}
