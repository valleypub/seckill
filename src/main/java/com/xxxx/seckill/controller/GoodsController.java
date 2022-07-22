package com.xxxx.seckill.controller;


import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IUserService;
import com.xxxx.seckill.vo.DetailVo;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IGoodsService goodsService;//用于查商品相关的信息

    @Autowired
    private RedisTemplate redisTemplate;//页面缓存使用

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;//用于手动渲染模板

/*   //第一种使用Spring Session的
    @RequestMapping("/toList")
    //HttpSession session 用于获取之前别的脚本中存进Session的信息
    public String toList(HttpSession session, Model model, @CookieValue("userTicket") String ticket){
        //先判断ticket是否为空，如果为空，说明没有登陆去登录(跳转到登录界面)
        if(StringUtils.isEmpty(ticket)){
            return "login";
        }
        //第一种：通过Spring Session存储，获取的方式
        User user = (User) session.getAttribute(ticket);
        if(null == user){
            return "login";
        }
        //都没有问题，就使用model将user对象传到前端去，再跳转到商品界面
        model.addAttribute("user", user);
        return "goodsList";
    }*/

/*    //第二种分布式的
    @RequestMapping("/toList")
    public String toList(HttpServletRequest request, HttpServletResponse response, Model model, @CookieValue("userTicket") String ticket){
        //先判断ticket是否为空，如果为空，说明没有登陆去登录(跳转到登录界面)
        if(StringUtils.isEmpty(ticket)){
            return "login";
        }
        //第一种：通过Spring Session存储，获取的方式
        User user = (User) userService.getUserByCookies(ticket, request, response);
        if(null == user){
            return "login";
        }
        //都没有问题，就使用model将user对象传到前端去，再跳转到商品界面
        model.addAttribute("user", user);
        return "goodsList";
    }*/


    /**
     * 使用拦截器处理方法输入参数的校验的
     * @param model
     * @param user
     * @return
     */
    @RequestMapping(value = "/toList", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList(Model model, User user,
                         HttpServletRequest request, HttpServletResponse response){
        //只将ticket的判断放在拦截器里了，但是接受的可能是null
        //如果接受的user == null，说明手机号码、password没错，但是Cookies不存在，这种情况会出现吗？？？
        // 因为手机号码、password输入不正确根本就出不了doLogin方法，会直接抛出异常，然后被全局异常捕获并处理给前端
        if(null == user){
            ////这个是直接返回版的
            //return "login";
            //这个是页面缓存版的
            ValueOperations valueOperations = redisTemplate.opsForValue();
            String loginHtml = (String) valueOperations.get("login"); //html页面是String类型
            if(!StringUtils.isEmpty(loginHtml)){
                return loginHtml;
            }
            else{//生成loginHtml页面并存进Redis中，这一步按理说不应该在在里做的

            }
        }

        //获取redis中的hrml
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList"); //html页面是String类型
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        //都没有问题，就使用model将user对象传到前端去，再跳转到商品界面
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        //return "goodsList";
        //如果html为空，手动渲染，存入Redis并返回
        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", context);
        if(!StringUtils.isEmpty(html)){
            valueOperations.set("goodsList", html, 60, TimeUnit.SECONDS);//存进去最好加一个失效时间，这样数据可以动态变化
        }
        return html;
    }



    /**
     * 使用模板引擎的跳转商品详情页
     * @param goodsId
     * @return
     */
    /*@RequestMapping(value = "/toDetail/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail(Model model, User user, @PathVariable Long goodsId,
                           HttpServletRequest request, HttpServletResponse response){

        //先去redis中进行查询
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsDetail:"+goodsId);
        if(!StringUtils.isEmpty(html)){
            return html;
        }

        //将数据放进View
        model.addAttribute("user", user);
        model.addAttribute("goods", goodsService.findGoodsVoByGoodsId(goodsId));
        //秒杀倒计时相关的
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate(); Date endDate = goodsVo.getEndDate(); Date nowDate = new Date();
        int remainSeconds = 0;
        int seckillStatus = 0;
        //秒杀未开始
        if(nowDate.before(startDate)){
            remainSeconds = (int)((startDate.getTime() - nowDate.getTime())/1000);
        }else if(nowDate.after(endDate)){
            //秒杀已结束
            seckillStatus = 2;
            remainSeconds = -1;
        }else{
            seckillStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("seckillStatus", seckillStatus);
        model.addAttribute("goods", goodsVo);
        model.addAttribute("remainSeconds", remainSeconds);
        //跳转到goodsDetail页面
        //return "goodsDetail";

        //如果redis中没查到对应id的详情页面，就：
        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", context);
        if(!StringUtils.isEmpty(html)){
            valueOperations.set("goodsDetail:"+goodsId, html, 60, TimeUnit.SECONDS);
        }
        return html;

    }*/


    /**
     * 前后端分离方式 的跳转商品详情页
     * @param model
     * @param user
     * @param goodsId
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/toDetail/{goodsId}")
    @ResponseBody
    public RespBean toDetail(Model model, User user, @PathVariable Long goodsId,
                             HttpServletRequest request, HttpServletResponse response){
        if(user == null){
            return RespBean.error(RespBeanEnum.USER_IS_NULL);
        }
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();
        //秒杀状态：0->尚未开始；1->正在进行中；2->已结束
        int seckillStatus = 0;
        int remainSeconds = 0;
        if(nowDate.before(startDate)){
            seckillStatus = 0;
            remainSeconds = (int) (startDate.getTime() - nowDate.getTime()) / 1000;
        }else if(nowDate.after(endDate)){
            seckillStatus = 2;
            remainSeconds = -1;
        }else{
            seckillStatus = 1;
            remainSeconds = 0;
        }
        DetailVo detailVo = new DetailVo(user, goodsVo, seckillStatus, remainSeconds);
        return RespBean.success(detailVo);
    }


}
