package com.example.manmu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class VoteService {

    @Autowired
    private RedisTemplate redisTemplate;


    public Integer setVoteCount(String roomId, Integer poll) {
        redisTemplate.opsForValue().increment(roomId, poll);
        redisTemplate.expire(roomId, 60, TimeUnit.SECONDS);
        return (Integer) redisTemplate.opsForValue().get(roomId);
    }

    public Integer setUserCount(String roomId) {
        redisTemplate.opsForValue().increment(roomId+"count");
        redisTemplate.expire(roomId+"count", 60, TimeUnit.SECONDS);
        return (Integer) redisTemplate.opsForValue().get(roomId+"count");
    }


    public Integer getMatchResult(String roomId) {
        Integer result = (Integer) redisTemplate.opsForValue().get(roomId);
        return result;
    }

}
