package com.example.practisejdbl69.service.impl;

import com.example.practisejdbl69.domain.Book;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisServiceSampleImpl {

    // Field redisTemplate in com.example.practisejdbl69.service.impl.BookServiceImpl required a bean of type 'org.springframework.data.redis.core.RedisTemplate' that could not be found.
    // configuration annotation is not set
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisServiceSampleImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addBook(Book book){

    }
}
