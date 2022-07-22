package com.xxxx.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor //自动根据变量创建无参构造方法
@AllArgsConstructor //自动根据变量创建全参构造方法
@Slf4j
public class RespBean {

    private  long code;
    private String message;
    private  Object obj;

    //成功的返回结果
    public static  RespBean success(){
        log.info("{}",RespBeanEnum.SUCCESS); //这一行是因为有的时候前端出错时，只会返回一个服务端异常，此时，就需要使用LomBok在后端处检测问题
        return new RespBean(RespBeanEnum.SUCCESS.getCode(), RespBeanEnum.SUCCESS.getMessage(), null);
    }
    public static  RespBean success(Object obj){
        log.info("{}",RespBeanEnum.SUCCESS);
        return new RespBean(RespBeanEnum.SUCCESS.getCode(), RespBeanEnum.SUCCESS.getMessage(), obj);
    }

    //失败时返回的
    //之所以直接传进去RespBeanEnum respBeanEnum，是因为成功只有200，但是失败却各有不同：500 404 等
    public static  RespBean error(RespBeanEnum respBeanEnum){
        log.info("{}",respBeanEnum);
        return new RespBean(respBeanEnum.getCode(), respBeanEnum.getMessage(), null);
    }

    public static  RespBean error(RespBeanEnum respBeanEnum, Object obj){
        log.info("{}",respBeanEnum);
        return new RespBean(respBeanEnum.getCode(), respBeanEnum.getMessage(), obj);
    }

}
