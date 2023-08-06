package city.roast.util;

import jakarta.xml.bind.DatatypeConverter;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptHelper {

    public static String md5(String inputStr) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] bytes = messageDigest.digest(inputStr.getBytes("UTF-8"));
            return DatatypeConverter.printHexBinary(bytes);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

}
