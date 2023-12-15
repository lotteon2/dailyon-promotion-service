package com.dailyon.promotionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@EnableJpaAuditing
@EnableEurekaClient
@SpringBootApplication
public class PromotionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PromotionServiceApplication.class, args);
	}

}
