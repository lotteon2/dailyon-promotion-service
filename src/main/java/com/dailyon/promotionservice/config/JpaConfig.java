package com.dailyon.promotionservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories( basePackages = {
        "com.dailyon.promotionservice.domain.coupon.repository",
        "com.dailyon.promotionservice.domain.event.repository",
        "com.dailyon.promotionservice.domain.raffle.repository.jpa"
})
public class JpaConfig {
}
