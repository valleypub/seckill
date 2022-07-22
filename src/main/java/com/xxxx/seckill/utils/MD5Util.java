package com.xxxx.seckill.utils;


import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

@Component
public class MD5Util {

    //
    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    //这个salt只是为了和前端的salt进行统一，因为第一次是在前端实际上加密的
    private static final String salt="1a2b3c4d";

    //第一次加密
    //这个和前端的salt、加密方式一摸一样，就是用来模拟前端加密的
    public static String inputPassToFormPass(String inputPass){
        //混淆salt
        String str = "" + salt.charAt(0)+salt.charAt(2)+inputPass+salt.charAt(5)+salt.charAt(4);
        //第一次加密
        return md5(str);
    }

    //第二次加密
    public static String formPassToDBPass(String formPass, String salt){
        String str = salt.charAt(0) + salt.charAt(2)+formPass+salt.charAt(5)+salt.charAt(4);
        return md5(str);

    }

    //后端实际上调用的是这个
    public static String inputPassToDBPass(String inputPass, String salt){
        String formPass = inputPassToFormPass(inputPass);
        String dbPass = formPassToDBPass(formPass, salt);
        return dbPass;
    }

    public static void main(String... args){
        System.out.println(inputPassToFormPass("kanghaiquan"));

        System.out.println(formPassToDBPass("eddf4106ce75124055a132c568a3a209", "1a2b3c4d"));

        System.out.println(inputPassToDBPass("kanghaiquan", "1a2b3c4d"));
    }

}
