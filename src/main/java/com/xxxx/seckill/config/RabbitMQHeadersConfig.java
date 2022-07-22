/*
package com.xxxx.seckill.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class RabbitMQHeadersConfig {

    private static final String QUEUE01 = "queue_header01";
    private static final String QUEUE02 = "queue_header02";
    private static final String EXCHANGE = "headersExchange";
    private static final String ROUTINGKEY01 = "#.queue.#";
    private static final String ROUTINGKEY02 = "*.queue.#";


    //1.创建队列
    @Bean
    public Queue queue01(){
        return new Queue(QUEUE01);
    }
    @Bean
    public Queue queue02(){
        return new Queue(QUEUE02);
    }

    //2.创建交换机
    @Bean
    public HeadersExchange headersExchange(){
        return new HeadersExchange(EXCHANGE);
    }

    //3.绑定
    @Bean
    public Binding binding01(){
        Map<String, Object> map = new HashMap<>();
        map.put("color","red");
        map.put("speed","low");
        return BindingBuilder.bind(queue01()).to(headersExchange()).whereAny(map).match();
    }
    @Bean
    public Binding binding02(){
        Map<String, Object> map = new HashMap<>();
        map.put("color","red");
        map.put("speed","fast");
        return BindingBuilder.bind(queue02()).to(headersExchange()).whereAll(map).match();
    }


}
*/
