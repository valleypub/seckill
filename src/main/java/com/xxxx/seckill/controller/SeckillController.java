package com.xxxx.seckill.controller;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wf.captcha.ArithmeticCaptcha;
import com.xxxx.seckill.config.AccessLimit;
import com.xxxx.seckill.exception.GlobalException;
import com.xxxx.seckill.pojo.Order;
import com.xxxx.seckill.pojo.SeckillMessage;
import com.xxxx.seckill.pojo.SeckillOrder;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.rabbitmq.MQSender;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IOrderService;
import com.xxxx.seckill.service.ISeckillOrderService;
import com.xxxx.seckill.utils.JsonUtil;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {

    @Autowired
    private IGoodsService goodsService; //用来查商品表的
    @Autowired
    private ISeckillOrderService seckillOrderService; //用来查秒杀订单表的
    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;//操作Redis的

    @Autowired
    private MQSender mqSender; //发送秒杀信息到mq队列

    /*@Autowired
    private RedisScript<Long> redisScript;//使用lua实现的redis分布式锁*/

    //用于内存标记，减少redis访问
    private Map<Long, Boolean> EmptyStockFlag = new HashMap<>();


    /**
     * 前后端不分离--使用thymeleaf方式的后端秒杀接口
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    /*@RequestMapping("/doSeckill")
    public String doSeckill(Model model, User user, Long goodsId){
        if(null == user){
            return "login";
        }
        //如果用户存在，把用户放进去
        model.addAttribute("user", user);
        //判断库存够不够：不能相信前端显示的库存数量，因为前端的可以随意改、等，库存数量只能使用goods.id去数据表里查询
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        if(goods.getStockCount() < 1){
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail"; //跳转到秒杀失败页面。
        }
        //判断秒杀订单：如果秒杀过，就不能再秒杀了
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        if(seckillOrder != null){
            model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage());
            return "secKillFail";
        }
        //运行到这里，说明：可以秒杀
        //1.生成订单
        Order order = orderService.seckill(user, goods);
        model.addAttribute("order", order);
        model.addAttribute("goods", goods);
        return "orderDetail";

    }*/


    /**
     * 使用前后端分离的秒杀接口
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/{path}/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill(@PathVariable String path, User user, Long goodsId){
        if(user == null){
            return RespBean.error(RespBeanEnum.USER_IS_NULL);
        }
        //判断前端传来的path
        ValueOperations valueOperations = redisTemplate.opsForValue();
        boolean check = orderService.checkPath(user, goodsId, path);
        if(!check){
            return RespBean.error(RespBeanEnum.PATH_ERROR);
        }

        //1、判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:"+user.getId()+":"+goodsId);
        if(seckillOrder != null) {
            return RespBean.error(RespBeanEnum.REPEAT_ERROR);
        }
        //内存标记减少redis的访问
        if(EmptyStockFlag.get(goodsId)){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //2、预减库存，使用原子性的操作
        Long stock = valueOperations.decrement("seckillGoods:"+goodsId);//获取预减后的库存
        //Long stock = (Long) redisTemplate.execute(redisScript, Collections.singletonList("seckillGoods:" + goodsId));
        if(stock < 0){
            //此商品的内存为空标记置为true
            EmptyStockFlag.put(goodsId, true);
            //先恢复为0再报错返回
            valueOperations.increment("seckillGoods:"+goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //3、运行到这里，说明：满足下单条件，开始下单
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
        //发完消息后立即返回0这个对象，前端从data的obj中取出0后显示"正在排队中"
        return RespBean.success(0);

    }

    @RequestMapping(value = "/getResult", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user, Long goodsId){
        if(null==user){
            return RespBean.error(RespBeanEnum.USER_IS_NULL);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }


    //
    @AccessLimit(second = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request){
        if(null == user){
            return RespBean.error(RespBeanEnum.USER_IS_NULL);
        }
//        //简单接口限流：
//        ValueOperations valueOperations = redisTemplate.opsForValue();
//        String uri = request.getRequestURI();
//        captcha = "0";//这里是随便给个值，这样前端验证码框里可以不填，方便简单测试一下
//        Integer count = (Integer) valueOperations.get(uri + ":" + user.getId());
//        if(count==null){
//            valueOperations.set(uri+":"+user.getId(), 1, 60, TimeUnit.SECONDS);//这里给出的是60s内计数来判断
//        }else if(count<5){
//            valueOperations.increment(uri+":"+user.getId());
//        }else{
//            return RespBean.error(RespBeanEnum.ACCESS_OUT_LIMIT);
//        }
        //做验证码校验
        boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if(!check){
            return RespBean.error(RespBeanEnum.CAPTCHA_EROOR);
        }
        String str = orderService.createPath(user, goodsId);
        //将这个str返回给前端
        return RespBean.success(str);
    }


    @RequestMapping(value = "/captcha", method = RequestMethod.GET)
    public void generateCaptcha(User user, Long goodsId, HttpServletResponse resp){
        if(user == null){
            throw new GlobalException(RespBeanEnum.USER_IS_NULL);
        }
        // 设置请求头为输出图片类型
        resp.setContentType("image/jpg");
        resp.setHeader("Pragma", "No-cache");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setDateHeader("Expires", 0);

        //生成算数类型验证码，并将结果放入redis
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);
        String answer = captcha.text();
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, answer, 300, TimeUnit.SECONDS);
        try {
            captcha.out(resp.getOutputStream());
        } catch (IOException e) {
            log.info("验证码生成失败。" + e.getMessage());
        }
    }


    /**
     * 初始化时执行的一些方法放在这里
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //从数据库加载商品列表
        List<GoodsVo> list = goodsService.findGoodsVo();
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        //如果不为空，将每种商品的库存入redis。这个语法很想C#中的并行循环
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:"+goodsVo.getId(), goodsVo.getStockCount());
            if(goodsVo.getStockCount() < 1){
                EmptyStockFlag.put(goodsVo.getId(), true);
            }else{
                EmptyStockFlag.put(goodsVo.getId(), false);
            }
        });
    }

}
