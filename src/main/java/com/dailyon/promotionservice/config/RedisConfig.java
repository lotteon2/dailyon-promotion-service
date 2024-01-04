package com.dailyon.promotionservice.config;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

  private final Environment env;

  @Bean
  @Profile(value = "!prod")
  public LettuceConnectionFactory standaloneRedisConnectionFactory() {
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setHostName(
        Objects.requireNonNull(env.getProperty("spring.redis.host")));
    redisStandaloneConfiguration.setPort(
        Integer.parseInt(Objects.requireNonNull(env.getProperty("spring.redis.port"))));
    redisStandaloneConfiguration.setPassword(env.getProperty("spring.redis.password"));

    return new LettuceConnectionFactory(redisStandaloneConfiguration);
  }

  @Bean
  @Profile("prod")
  public RedisConnectionFactory clusterRedisConnectionFactory() {
    RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration();
    clusterConfiguration.setClusterNodes(
        parseRedisNodes(Objects.requireNonNull(env.getProperty("spring.redis.cluster.nodes"))));

    return new LettuceConnectionFactory(clusterConfiguration);
  }

  private Set<RedisNode> parseRedisNodes(String nodes) {
    Set<RedisNode> redisNodes = new HashSet<>();
    for (String node : Objects.requireNonNull(nodes).split(",")) {
      String[] parts = node.split(":");
      redisNodes.add(new RedisNode(parts[0], Integer.parseInt(parts[1])));
    }
    return redisNodes;
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new StringRedisSerializer());

    boolean isCluster = Objects.nonNull(env.getProperty("spring.redis.cluster.nodes"));
    if (!isCluster) {
      redisTemplate.setConnectionFactory(standaloneRedisConnectionFactory());
    } else {
      redisTemplate.setConnectionFactory(clusterRedisConnectionFactory());
    }
    return redisTemplate;
  }
}
