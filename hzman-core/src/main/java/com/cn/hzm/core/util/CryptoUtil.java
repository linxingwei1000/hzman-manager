package com.cn.hzm.core.util;

import com.cn.hzm.core.constant.ResponseCode;
import com.cn.hzm.core.exception.HzmBasicRuntimeException;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Created by yuyang04 on 2021/1/17.
 */
public class CryptoUtil {

    public static String encryptWithAesEcbBase64(String plainText, String secret) throws Exception {
        if (StringUtils.isBlank(plainText) || StringUtils.isBlank(secret)) {
            throw new HzmBasicRuntimeException(ResponseCode.SERVER_ERROR);
        }

        byte[] key = secret.getBytes();
        byte[] data = plainText.getBytes();

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        return Base64.getEncoder().encodeToString(cipher.doFinal(data));
    }

    public static String decryptWithAesEcbBase64(String encrypt, String secret) throws Exception {
        if (StringUtils.isBlank(encrypt) || StringUtils.isBlank(secret)) {
            throw new HzmBasicRuntimeException(ResponseCode.SERVER_ERROR);
        }

        byte[] key = secret.getBytes();
        byte[] data = Base64.getDecoder().decode(encrypt);

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        return new String(cipher.doFinal(data));
    }
}
