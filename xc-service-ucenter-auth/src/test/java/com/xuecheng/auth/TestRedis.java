package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRedis {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Test
    public void testRedis() {

        //定义key
        String key = "fa13651b-eade-4c45-9770-efcf676c6c58";
        //定义value
        Map<String, String> value = new HashMap<>();
        value.put("jwt", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6Iml0Y2FzdCIsInNjb3BlIjpbImFwcCJdLCJuYW1lIjpudWxsLCJ1dHlwZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTYxMTAzNjYwMCwianRpIjoiZmExMzY1MWItZWFkZS00YzQ1LTk3NzAtZWZjZjY3NmM2YzU4IiwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ.PPRztvD_b8KptvQPeyDU-oZMnwhO1FPKnQjzX68aY0vDJSK2AkYBKZhPxSzBuZpYuZ5Da4HhU_9_lYRU0DmwC0dm4f9G3pje8FNmgVPlmquS3bTKncu5V2LYdr9BKHMs7T97NNrGjlEY3bl8NqVz1nVpXuy3Mm4rJW7F-pOzJxryG0A89VV_YuRwGEAvEGK-ZrorXrons2hN9xelFgge2NLHY5kx-QBBXY01lurkyIxlCzEQG9J6sujWf-_FyGHCY3UstDOkkOkipZpwtKXZ5n8D8JC_Gf1iQL3fbc6hJ_QAYHaQW2_qB2JLeGonQxgEqN7SSnTrKMyULis30k5Gog");
        value.put("refresh_token", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6Iml0Y2FzdCIsInNjb3BlIjpbImFwcCJdLCJhdGkiOiJmYTEzNjUxYi1lYWRlLTRjNDUtOTc3MC1lZmNmNjc2YzZjNTgiLCJuYW1lIjpudWxsLCJ1dHlwZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTYxMTAzNjYwMCwianRpIjoiOGRmODE1NjUtMDgyOC00ZjMwLTk5MjUtZDJhOTgxZjk0M2Y1IiwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ.NBpnaF212i9iIS1T_2AFRFNygY-3-PKW1z_XdPnNvQNNhQLF8g-EFEyezJF0_IUem6kJ0T9VuvyL9im39-KhU9I8MugHf9u-q3uC8-QHIzzOU9AwPobRNQJf6CYFJMkSRxw6P1WNuVT2VBVYsQyW3vEgyPfpbzt-i9-64mSi420oVRlWovS7ANrIX5_K74Amz0A_xLZnTHAlotZVZz1qicejYrRliN-v0ziEtC5OPQgAJQMpv69eFB3QeGMyn1_NEcUcCznCu_31E6x9p4j0MHZuUBp2euwy7yvjYZvF51Q5b9S2iHFZNDvNnCSFHV2-kWfZjlbaWizbBhFoorzAXg");
        String jsonString = JSON.toJSONString(value);
        //校验key是否存在..如果不存在，则返回-2
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        System.out.println(expire);

        //存储数据
        redisTemplate.boundValueOps(key).set(jsonString, 30, TimeUnit.SECONDS);
        //获取数据
        String s = redisTemplate.opsForValue().get(key);
        System.out.println(s);
    }

}
