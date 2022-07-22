package com.xxxx.seckill.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SeckillRabbitMQTopicConfig {

    private static final String QUEUE = "seckillQueue";
    private static final String EXCHANGE = "seckillExchange";

    //1.创建队列
    @Bean
    public Queue queue(){
        return new Queue(QUEUE);
    }

    //2.创建交换机
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(EXCHANGE);
    }

    //3.绑定
    @Bean
    public Binding binding(){
        return BindingBuilder.bind(queue()).to(topicExchange()).with("seckill.#");
    }


}
