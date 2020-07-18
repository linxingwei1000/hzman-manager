package com.cn.hzm.core.aws;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;


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

}
