package com.xxxx.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.seckill.exception.GlobalException;
import com.xxxx.seckill.mapper.OrderMapper;
import com.xxxx.seckill.pojo.Order;
import com.xxxx.seckill.pojo.SeckillGoods;
import com.xxxx.seckill.pojo.SeckillOrder;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IOrderService;
import com.xxxx.seckill.service.ISeckillGoodsService;
import com.xxxx.seckill.service.ISeckillOrderService;
import com.xxxx.seckill.utils.MD5Util;
import com.xxxx.seckill.utils.UUIDUtil;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.OrderDetailVo;
import com.xxxx.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author kanghaiquan
 * @since 2022-06-23
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private ISeckillGoodsService seckillGoodsService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;


    @Transactional//事务注解
    @Override
    public Order seckill(User user, GoodsVo goods) {
        //1.先拿到秒杀商品
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goods.getId()));
        /*boolean seckillGoodsResult = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().set("stock_count",
                seckillGoods.getStockCount()).eq("id", seckillGoods.getId()).gt("stock_count", 0));
        //seckillGoodsService.updateById(seckillGoods);
        //如果没有更新成功
        if(!seckillGoodsResult){
            return null;
        }*/
        //对数据库行锁型减1
        boolean result = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>()
                .setSql("stock_count = stock_count - 1")
                .eq("id", seckillGoods.getId())
                .gt("stock_count", 0));
//        //如果减1失败 == 下单失败
//        if(!result){
//            return null;
//        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        if(seckillGoods.getStockCount()<1){
            //在使用mq时，前端轮询时需要用到isStockEmpty
            valueOperations.set("isStockEmpty:"+goods.getId(), "0");
            return null;
        }
        //2.生成订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goods.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        orderMapper.insert(order);
        //3.生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(goods.getId());
        seckillOrderService.save(seckillOrder);
        //将订单存进redis
        redisTemplate.opsForValue().set("order:"+user.getId()+":"+goods.getId(), seckillOrder);
        //4.返回订单
        return order;
    }


    @Override
    public OrderDetailVo detail(Long orderId) {
        if(orderId == null){
            throw new GlobalException(RespBeanEnum.ORDER_UNEXISTS);
        }
        Order order = orderMapper.selectById(orderId);
        //根据订单查询对应的商品的信息
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());
        return new OrderDetailVo(order, goodsVo);
    }

    /**
     * 获取秒杀地址
     * @param user
     * @param goodsId
     * @return
     */
    @Override
    public String createPath(User user, Long goodsId) {
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        //将str存进redis中用于后期作校验
        redisTemplate.opsForValue().set("seckillPath:" + user.getId() + ":" + goodsId, str, 60, TimeUnit.SECONDS);
        return str;
    }

    @Override
    public boolean checkPath(User user, Long goodsId, String path) {
        if(user == null || StringUtils.isEmpty(path)){
            return false;
        }
        String redisPath = (String) redisTemplate.opsForValue().get("seckillPath:"+user.getId()+":"+goodsId);
        return path.equals(redisPath);
    }

    @Override
    public boolean checkCaptcha(User user, Long goodsId, String captcha) {
        if(user == null || StringUtils.isEmpty(captcha)){
            return false;
        }
        String redisCode = (String) redisTemplate.opsForValue().get("captcha:" + user.getId() + ":" + goodsId);
        if(StringUtils.isEmpty(redisCode) || !redisCode.equals(captcha)){
            return false;
        }
        return true;
    }

}
