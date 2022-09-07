package com.cn.hzm.core.aws;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;


/**
 * @author xingweilin@clubfactory.com
 * @date 2020/6/28 11:31 下午
 */
public class Encrypt {

    private static final String CHARACTER_ENCODING = "UTF-8";

    final static String ALGORITHM = "HmacSHA256";

    private static final String SECRET_KEY = "+FcHAPWH99nrP/sorLOlTFwlAiDG/H6EQvIc7rMW";

    public static String sign(String data){
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(SECRET_KEY.getBytes(CHARACTER_ENCODING), ALGORITHM));
            byte[] signature = mac.doFinal(data.getBytes(CHARACTER_ENCODING));
            return new String(Base64.encodeBase64(signature), CHARACTER_ENCODING);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] hmacsha256(byte[] key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    public static String hmacsha256WithHex(byte[] key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] array = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return byte2Hex(array);
    }

    public static String getSHA256Str(String str){
        MessageDigest messageDigest;
        String encodeStr = "";
        try{
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
            encodeStr = byte2Hex(messageDigest.digest());
        }catch (Exception e){
            e.printStackTrace();
        }
        return encodeStr;
    }

    public static String byte2Hex(byte[] bytes){
        StringBuilder stringBuilder = new StringBuilder();
        String temp = null;
        for(int i=0;i< bytes.length;i++){
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if(temp.length()==1){
                stringBuilder.append("0");
            }
            stringBuilder.append(temp);
        }
        return stringBuilder.toString();
    }

}
