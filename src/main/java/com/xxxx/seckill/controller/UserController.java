package com.xxxx.seckill.controller;


import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.rabbitmq.MQSender;
import com.xxxx.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.ParameterResolutionDelegate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author kanghaiquan
 * @since 2022-06-20
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private MQSender mqSender;

    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user) {
        return RespBean.success(user);
    }

    /**
     * 测试rabbitmq的发送功能
     */
    @RequestMapping("/mq")
    @ResponseBody
    public void mq(){
        mqSender.send("hello kanghaiquan!!!");
    }

    /**
     * 测试rabbitmq的发送功能
     */
    @RequestMapping("/mq/fanout")
    @ResponseBody
    public void mq_fanout(){
        mqSender.send_fanout("fanout: hello kanghaiquan!!!");
    }

    /**
     * 测试rabbitmq的发送功能
     */
    @RequestMapping("/mq/direct_red")
    @ResponseBody
    public void mq_direct_red(){
        mqSender.send_direct_red("direct: red");
    }
    @RequestMapping("/mq/direct_green")
    @ResponseBody
    public void mq_direct_green(){
        mqSender.send_direct_green("direct: green");
    }


    @RequestMapping("/mq/topic01")
    @ResponseBody
    public void mq_topic01(){
        mqSender.send_topic01("topic01");
    }
    @RequestMapping("/mq/topic02")
    @ResponseBody
    public void mq_topic02(){
        mqSender.send_topic02("topic02");
    }

    @RequestMapping("/mq/header01")
    @ResponseBody
    public void mq_header01(){
        mqSender.send_headers01("header01");
    }
    @RequestMapping("/mq/header02")
    @ResponseBody
    public void mq_header02(){
        mqSender.send_headers02("header02");
    }

}
