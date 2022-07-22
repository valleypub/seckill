package com.xxxx.seckill.rabbitmq;


import com.xxxx.seckill.pojo.SeckillMessage;
import com.xxxx.seckill.pojo.SeckillOrder;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IOrderService;
import com.xxxx.seckill.utils.JsonUtil;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 消息的消费者
 */
@Service
@Slf4j
public class MQReceiver {

    @Autowired
    private IGoodsService goodsService;//用于秒杀下单查询数据库
    @Autowired
    private RedisTemplate redisTemplate;//操作redis
    @Autowired
    private IOrderService orderService;//用于操作订单表下单的

    @RabbitListener(queues = "queue") //需要使用这个注解来指定监听哪个队列
    public void receive(Object msg){
        log.info("接收消息："+msg);
    }

    @RabbitListener(queues = "queue_fanout01")
    public void receive01(Object msg){
        log.info("QUEUE01接收到消息："+msg);
    }
    @RabbitListener(queues = "queue_fanout02")
    public void receive02(Object msg){
        log.info("QUEUE02接收到消息："+msg);
    }

    @RabbitListener(queues = "queue_direct01")
    public void receive03(Object msg){
        log.info("QUEUE01接收到消息："+msg);
    }
    @RabbitListener(queues = "queue_direct02")
    public void receive04(Object msg){
        log.info("QUEUE02接收到消息："+msg);
    }

    @RabbitListener(queues = "queue_topic01")
    public void receive05(Object msg){
        log.info("QUEUE01接收到消息："+msg);
    }
    @RabbitListener(queues = "queue_topic02")
    public void receive06(Object msg){
        log.info("QUEUE02接收到消息："+msg);
    }


    @RabbitListener(queues = "queue_header01")
    public void receive07(Message message){
        log.info("QUEUE01接收到Message对象："+message);
        log.info("QUEUE01接收到消息："+new String(message.getBody()));
    }
    @RabbitListener(queues = "queue_header02")
    public void receive08(Message message){
        log.info("QUEUE02接收到Message对象："+message);
        log.info("QUEUE02接收到消息："+new String(message.getBody()));
    }


    //秒杀相关的消息消费
    @RabbitListener(queues = "seckillQueue")//方法监听seckillQueue队列
    public void receive09(String message){
        log.info("从seckillQueue接收到消息："+message);
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(message, SeckillMessage.class);
        Long goodId = seckillMessage.getGoodId();
        User user = seckillMessage.getUser();
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodId);
        if(goodsVo.getStockCount()<1){
            return;
        }
        //判断是否重复抢购，为什么这里还要进行一次？？消息入mq之前不是已经判断一次了吗？？
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:"+user.getId()+":"+goodId);
        if(seckillOrder != null){
            return;
        }
        //下单操作
        orderService.seckill(user, goodsVo);


    }



}
