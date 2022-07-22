package com.xxxx.seckill.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IUserService;
import com.xxxx.seckill.utils.CookieUtil;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class AccessLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private IUserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(handler instanceof HandlerMethod){

            User user = getUser(request, response);
            //将user对象放入ThreadLocal中
            UserContext.setUser(user);

            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            //如果没有这个注解，直接跳过拦截。
            if(accessLimit == null){
                return true;
            }else{
                //如果有这个注解，我需要拿到：秒数、最大计数、是否需要登录
                int second = accessLimit.second();
                int maxCount = accessLimit.maxCount();
                boolean needLogin = accessLimit.needLogin();

                //简单接口限流：
                //ValueOperations valueOperations = redisTemplate.opsForValue();
                String uri = request.getRequestURI();
                if(needLogin){
                    if(user==null){
                        //返回给前端对象信息
                        render(request, response, RespBeanEnum.USER_IS_NULL);
                        return false;
                    }
                    //captcha = "0";//这里是随便给个值，这样前端验证码框里可以不填，方便简单测试一下
                    ValueOperations valueOperations = redisTemplate.opsForValue();
                    Integer count = (Integer) valueOperations.get(uri + ":" + user.getId());
                    if(count==null){
                        valueOperations.set(uri+":"+user.getId(), 1, 60, TimeUnit.SECONDS);//这里给出的是60s内计数来判断
                    }else if(count<5){
                        valueOperations.increment(uri+":"+user.getId());
                    }else{
                        //返回给前端对象信息
                        render(request, response, RespBeanEnum.ACCESS_OUT_LIMIT);
                        return false;
                    }
                }

            }
        }
        return true;

    }

    //返回给前端对象信息
    private void render(HttpServletRequest request, HttpServletResponse response, RespBeanEnum respBeanEnum) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        RespBean respBean = RespBean.error(respBeanEnum);
        writer.write(new ObjectMapper().writeValueAsString(respBean));
        writer.flush();
        writer.close();
    }

    //根据request response对象获取当前登录的user对象
    private User getUser(HttpServletRequest request, HttpServletResponse response) {
        String ticket = CookieUtil.getCookieValue(request, "userTicket");
        //先判断ticket是否为空，如果为空，说明没有登陆去登录(跳转到登录界面)
        if(StringUtils.isEmpty(ticket)){
            return null;
        }
        return userService.getUserByCookies(ticket, request, response);
    }
}
