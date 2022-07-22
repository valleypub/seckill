/*
package com.xxxx.seckill.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class RabbitMQFanoutConfig {

    //fanout相关的
    private static final String QUEUE01 = "queue_fanout01";
    private static final String QUEUE02 = "queue_fanout02";
    private static final String EXCHANGE = "fanoutExchange";


    @Bean
    public Queue queue(){
        return new Queue("queue", true);//durable是配置持久化的意思
    }


    //生成队列
    @Bean
    public Queue queue01(){
        return new Queue(QUEUE01);
    }
    @Bean
    public Queue queue02(){
        return new Queue(QUEUE02);
    }

    //生成交换机
    @Bean
    public FanoutExchange fanoutExchange(){
        return new FanoutExchange(EXCHANGE);
    }

    //将队列绑定到交换机上去
    @Bean
    public Binding binding01(){
        return BindingBuilder.bind(queue01()).to(fanoutExchange());
    }
    @Bean
    public Binding binding02(){
        return BindingBuilder.bind(queue02()).to(fanoutExchange());
    }


}
*/
