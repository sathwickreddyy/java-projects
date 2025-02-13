package com.example.practisejdbl69.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {
    /*
     * Caching -> Redis
     *
     * Concept of fast access to some resource is simply known as caching. not used for persistence but for faster access.
     * Same concept is used in WebApplication.
     *
     * Say
     *
     * client -> Web App -> React Server -> Backend Service -> DB
     *
     * Say for to render home page : request goes to react server -> backend service -> DB
     * Network Latency : 1s
     * DB latency : 3 s
     *
     * Just to render home page: it will take 4s and if he logs back in again if it loads with same latency then
     * this will be bad customer experience.
     * Id there is a cache between frontend and webapp then it will be faster access.
     *
     * Pros :
     * - Latency gets reduced (Faster Access after a miss) ( It's saves the search latency from DB but the request latency remains the same )
     *
     *
     * Cons :
     * - When data gets updated in DB, the redis might hold previous version of data
     *
     * Connection with redis.
     *
     * 1. Dependency
     * 2. application property -> Setup the connection (Driver)
     *      i) Lettuce (default)
     *      ii) Jedis
     * 3. create a connection a bean
     * 4. create a template to access the data.
     */

    @Bean
    public LettuceConnectionFactory getLettuceConnectionFactory() {
        // redis supports 2 types: standalone(centralised) and cluster(distributed)
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration("redis-10192.c212.ap-south-1-1.ec2.redns.redis-cloud.com", 10192);
        redisStandaloneConfiguration.setUsername("default");
        redisStandaloneConfiguration.setPassword("PnvhV5ltHAcJPSjFAru2S9sfj0gsqomv");
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration);
        return lettuceConnectionFactory;
    }

    @Bean
    // Construction Injection of lettuceConnectionFactory because spring takes care while creating the bean
    public RedisTemplate<String, Object> getRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        //  Template is basically application passes the data like packets of data / container of data
        //  and this packet/container is called RedisTemplate
        //  SpringBoot Application will send the redis template to redis service which will be understood by redis server.
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        //  redisTemplate.setConnectionFactory(getLettuceConnectionFactory()); // creates a new object as we are calling method.
        redisTemplate.setKeySerializer(new StringRedisSerializer()); // inorder to store the keys as string rather than byte code of string
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
//        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class)); // Json object will be stored
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        return redisTemplate;
    }

    /*
     * Data eviction policy : Redis
     *       @Reference: https://redis.io/docs/latest/develop/reference/eviction/
     *       -> volatile-lru : if we don't set any expiry, then it will work as noeviction
     *
     *
     */
}
