package com.dailyon.promotionservice;

import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

//@ActiveProfiles("local")
//@ActiveProfiles("test")
//@Configuration
//public class RedisTestContainers {
//
//    private static final String REDIS_DOCKER_IMAGE = "redis:5.0.3-alpine";
//
//    static {
//        GenericContainer<?> REDIS_CONTAINER =
//                new GenericContainer<>(DockerImageName.parse(REDIS_DOCKER_IMAGE))
//                        .withExposedPorts(6379)
//                        .withReuse(true);
//
//        REDIS_CONTAINER.start();
//
//        System.out.println("테스트 컨테이너 시작");
//        System.out.println(REDIS_CONTAINER.getHost());
//        System.out.println(REDIS_CONTAINER.getMappedPort(6379).toString());
//        System.setProperty("spring.redis.host", REDIS_CONTAINER.getHost());
//        System.setProperty("spring.redis.port", REDIS_CONTAINER.getMappedPort(6379).toString());
//    }
//}