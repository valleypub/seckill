package com.xxxx.seckill.utils;

import org.thymeleaf.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//用来进行各种校验的：如：手机号码校验
public class ValidationUtil {

    private static final Pattern mobile_pattern = Pattern.compile("[1]([3-9])[0-9]{9}$");

    public static  boolean isMobile(String mobile){
        if(StringUtils.isEmpty(mobile)){
            return false;
        }
        Matcher matcher = mobile_pattern.matcher(mobile);
        return matcher.matches();
    }

}
