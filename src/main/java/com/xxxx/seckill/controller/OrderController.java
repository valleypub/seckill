package com.xxxx.seckill.controller;


import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IOrderService;
import com.xxxx.seckill.vo.OrderDetailVo;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author kanghaiquan
 * @since 2022-06-23
 */
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @RequestMapping("/detail")
    @ResponseBody
    public RespBean detail(User user, Long orderId){
        //因为WebConfig对User类型的拦截结果可能返回user == null这种情况，所以需要在这里再做一步判断
        if(user == null){
            return RespBean.error(RespBeanEnum.USER_IS_NULL);
        }

        //根据订单id查询订单具体数据封装成OrderDetailVo类型对象返回给前端
        OrderDetailVo detail = orderService.detail(orderId);
        return RespBean.success(detail);

    }


}
