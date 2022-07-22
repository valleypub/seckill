package com.xxxx.seckill.config;


import org.apache.ibatis.reflection.Jdk;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, Object> redisTemplate =  new RedisTemplate<String, Object>();

        //设置redis Key的序列化
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //其实redis默认的是jdk的一个序列化new JdkSerializationRedisSerializer()，产生的是二进制
        //还可以使用new Jackson2JsonRedisSerializer<Object>()，要传Object进去、产生一个Json对象(即，java字符串)
        //new GenericJackson2JsonRedisSerializer()，这个序列化完后也是产生一个Jason对象，但是不需要传一个Object对象。
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        // hash类型 key的序列化
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        // hash类型 value的序列化
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        //注入连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;

    }

//    //测试redis分布式锁的
//    @Bean
//    public DefaultRedisScript<Boolean> script(){
//        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
//        //lua脚本的位置，和application.yml同级目录
//        redisScript.setLocation(new ClassPathResource("lock.lua"));
//        redisScript.setResultType(Boolean.class);
//        return redisScript;
//    }

    @Bean
    public DefaultRedisScript<Long> script(){
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        //lua脚本的位置，和application.yml同级目录
        redisScript.setLocation(new ClassPathResource("stock.lua"));
        redisScript.setResultType(Long.class);
        return redisScript;
    }

}
