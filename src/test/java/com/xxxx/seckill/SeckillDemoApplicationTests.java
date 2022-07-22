package com.xxxx.seckill;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class SeckillDemoApplicationTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisScript<Boolean> redisScript;

    @Test
    public void testLock1() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 占位，只有key不存在才可以设置
        boolean isSet = valueOperations.setIfAbsent("k1", "v1");
        if(isSet){
            valueOperations.set("name", "xxxx");
            String name = (String) valueOperations.get("name");
            System.out.println(name);
            redisTemplate.delete("name");
        }else{
            System.out.println("有其他线程尚在使用，请稍后再试");
        }
    }

    @Test
    public void testLock2() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 给过程锁添加一个失效时间，防止锁无法释放
        boolean isSet = valueOperations.setIfAbsent("k1", "v1", 5, TimeUnit.SECONDS);
        if(isSet){
            valueOperations.set("name", "xxxx");
            String name = (String) valueOperations.get("name");
            System.out.println(name);
            Integer.parseInt("xxx");
            redisTemplate.delete("name");
        }else{
            System.out.println("有其他线程尚在使用，请稍后再试");
        }
    }

    @Test
    public void testLock3() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 占位，只有key不存在才可以设置
        String value = String.valueOf(UUID.randomUUID());
        boolean isSet = valueOperations.setIfAbsent("k1", value, 1200, TimeUnit.SECONDS);
        if(isSet){
            valueOperations.set("name", "xxxx");
            String name = (String) valueOperations.get("name");
            System.out.println(name);
            System.out.println(valueOperations.get("k1"));
            Boolean result = (Boolean) redisTemplate.execute(redisScript, Collections.singletonList("k1"), value);
            System.out.println(result);
        }else{
            System.out.println("有其他线程尚在使用，请稍后再试");
        }
    }
}
