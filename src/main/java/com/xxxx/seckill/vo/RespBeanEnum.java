package com.xxxx.seckill.vo;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;


@Getter //自动为每个变量生成get变量名()的方法
@ToString
@AllArgsConstructor //全参构造
public enum RespBeanEnum {
    //通用
    SUCCESS(200,"SUCCESS"),
    ERROR(500,"服务端异常"),
    //登录模块
    LOGIN_ERROR(500210, "用户名登录密码不正确"),
    MOBILE_ERROR(500211, "手机号码不正确"),
    MOBILE_NOT_EXIT(500213, "用户账号不存在"),
    PASSWORD_UPDATE_FAIL(500214, "更新密码失败"),
    USER_IS_NULL(500215, "user对象为null"),
    //异常相关
    BIND_ERROR(500212, "参数校验异常"),
    //
    DOMAIN_ERROR(500220, "Cookies域名错误"),
    //秒杀模块
    //
    REPEAT_ERROR(500501, "重复秒杀"),
    //空库存
    EMPTY_STOCK(500500, "库存不足"),
    //订单相关的
    ORDER_UNEXISTS(500600, "订单不存在"),
    //安全优化相关的
    PATH_ERROR(500700, "path不对，非法请求"),
    CAPTCHA_EROOR(500710, "验证码输入错误"),
    //限流相关的
    ACCESS_OUT_LIMIT(500800, "超出限流值");


    //状态码
    private final Integer code;
    //相应的状态信息
    private final String message;

    

}
