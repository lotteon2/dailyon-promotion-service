package com.dailyon.promotionservice.config;


import com.dailyon.promotionservice.common.util.RedisDistributedLockManager;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory lettuceConnectionFactory =
                new LettuceConnectionFactory(redisHost, redisPort);
        lettuceConnectionFactory.setPassword(redisPassword);
        return lettuceConnectionFactory;
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        // Note: It's typically a good practice to use "rediss://" (note the extra 's') for
        //       SSL/TLS-secured Redis connections. Update your URI scheme accordingly if needed.
        String redisUrl = "redis://" + redisHost + ":" + redisPort;
        config.useSingleServer().setAddress(redisUrl);

        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.useSingleServer().setPassword(redisPassword);
        }

        return Redisson.create(config);
    }

    // RedisLockConfig 설정 대신해주는 Bean
    @Bean
    public RedisDistributedLockManager redisDistributedLockManager(RedissonClient redissonClient) {
        return new RedisDistributedLockManager(redissonClient);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        return redisTemplate;
    }
}