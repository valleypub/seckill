package com.xxxx.seckill.controller;

import com.xxxx.seckill.mapper.UserMapper;
import com.xxxx.seckill.service.IUserService;
import com.xxxx.seckill.vo.LoginVo;
import com.xxxx.seckill.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
@Slf4j //Lombook的一个注解，用来输出日志的：
public class LoginController {

    @Autowired //用来使用IUserService接口
    private IUserService userService;


    //跳转登陆页面
    @RequestMapping("/toLogin")
    public String toLogin(){
        return "login";
    }

    //使用公共返回对象
    @RequestMapping("/doLogin")
    @ResponseBody //由于返回的是个Bean 而不是视图(xxx.html等)，所以需要使用@ResponseBody来限定，否则默认是@Controller返回对应的试图
    public RespBean doLogin(@Valid LoginVo loginVo, HttpServletRequest request, HttpServletResponse response){
        //log.info("{}",loginVo);  //只先使用LomBok来输出打印测试下
        //return null;
        return userService.doLogin(loginVo, request, response);
    }





}
