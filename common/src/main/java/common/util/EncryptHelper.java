package common.util;

import jakarta.xml.bind.DatatypeConverter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class EncryptHelper {

    public static String md5(String inputStr) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] bytes = messageDigest.digest(inputStr.getBytes(StandardCharsets.UTF_8));
            return DatatypeConverter.printHexBinary(bytes);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

}
