package com.xxxx.seckill.utils;

import java.util.UUID;


public class UUIDUtil {

    public static String uuid() {
        //使用jdk中的uuid来生成uuid然后将短横杠替换掉
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static void main(String[] args) {
        System.out.println(uuid());
    }

}
