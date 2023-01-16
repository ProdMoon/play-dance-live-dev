package com.example.manmu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class VoteService {

    private RedisTemplate redisTemplate;

    @Autowired
    public VoteService(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public void setMatchInfo(String key, Object field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
        redisTemplate.expire(key, 6000, TimeUnit.SECONDS);
    }

}
