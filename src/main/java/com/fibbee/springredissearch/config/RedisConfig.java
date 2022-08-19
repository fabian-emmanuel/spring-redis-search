package com.fibbee.springredissearch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Data
@Configuration
@ConfigurationProperties(prefix = "redis")
public class RedisConfig {
    private String host;
    private Integer port;

    @Bean
    JedisPooled jedisPooled(){
        return new JedisPooled(host, port);
    }
}
