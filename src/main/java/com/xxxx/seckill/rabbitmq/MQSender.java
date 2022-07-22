package com.xxxx.seckill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;


/**
 * 消息的生产者
 */
@Service
@Slf4j
public class MQSender {

    @Autowired
    //用来操控rabbitMq的，和RedisTemplate操控redis有点像
    private RabbitTemplate rabbitTemplate;

    public void send(Object msg){

        log.info("发送消息："+msg);
        //往queue这个队列发送消息
        rabbitTemplate.convertAndSend("queue", msg);//发送到名为queue的队列

    }

    //fanout模式的
    public void send_fanout(Object msg){

        log.info("发送消息："+msg);
        //往queue这个队列发送消息
        rabbitTemplate.convertAndSend("fanoutExchange", "", msg);//发送到名为fanoutExchage的交换机
    }

    //direct模式的
    public void send_direct_red(Object msg){
        log.info("发送red消息："+msg);
        rabbitTemplate.convertAndSend("directExchange", "queue.red", msg);
    }
    public void send_direct_green(Object msg){
        log.info("发送green消息："+msg);
        rabbitTemplate.convertAndSend("directExchange", "queue.green", msg);
    }

    //topic模式的
    public void send_topic01(Object msg){
        log.info("发送topic消息："+msg);
        rabbitTemplate.convertAndSend("topicExchange", "queue.topic.message", msg);
    }
    public void send_topic02(Object msg){
        log.info("发送topic消息："+msg);
        rabbitTemplate.convertAndSend("topicExchange", "message.queue.a.b.c", msg);
    }


    //headers模式的
    public void send_headers01(String msg){
        log.info("发送headers消息："+msg);
        MessageProperties properties = new MessageProperties();
        properties.setHeader("color", "red");
        Message message = new Message(msg.getBytes(), properties);
        rabbitTemplate.convertAndSend("headersExchange", "", message);
    }
    public void send_headers02(String msg){
        log.info("发送headers消息："+msg);
        MessageProperties properties = new MessageProperties();
        properties.setHeader("color", "red");
        properties.setHeader("speed", "fast");
        Message message = new Message(msg.getBytes(), properties);
        rabbitTemplate.convertAndSend("headersExchange", "", message);
    }


    /**
     * 发送秒杀信息
     * @param message
     */
    public void sendSeckillMessage(String message){
        log.info("发送消息"+message);
        rabbitTemplate.convertAndSend("seckillExchange", "seckill.message", message);
    }



}
