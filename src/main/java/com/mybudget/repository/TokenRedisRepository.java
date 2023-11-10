package com.mybudget.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class TokenRedisRepository {
    private final StringRedisTemplate redisTemplate;
    public void save(String key, String value) {
        redisTemplate.opsForValue().set(key, value , 7 , TimeUnit.DAYS);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
