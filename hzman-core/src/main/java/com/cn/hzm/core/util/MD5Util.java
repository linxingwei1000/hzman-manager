package com.cn.hzm.core.util;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/27 12:00 下午
 */
public class MD5Util {

    /**
     * 密钥
     */
    private static final String SALT = "hzmyzf";

    public static String md5(String t){
        return DigestUtils.md5Hex(t);
    }

    /**
     * 前端输入的密码到表单提交的密码(第一次加密) 用于校验前端传入的密码与该方法返回的密码是否相等
     */
    public static String inputPassToFormPass(String pass){
        String s = SALT + pass ;
        return md5(s);
    }

    /**
     * 表单提交的密码(第一次加密的密码)到数据库存储的密码(第二次加密)
     */
    public static String formPassToDb(String formPass){
        String s = formPass + SALT;
        return md5(s);
    }

    /**
     * 前端输入的明文密码到数据库存储的密码 可用于注册
     */
    public static String inputPassToDbPass(String input){
        String form = inputPassToFormPass(input);
        return formPassToDb(form);
    }

    public static void main(String[] args) {
        String password = "123456";
        String prePassword = inputPassToFormPass(password);
        String afterPassword = inputPassToDbPass(password);

        System.out.println(prePassword);
        System.out.println(afterPassword);
    }
}
