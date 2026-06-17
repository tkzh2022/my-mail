package com.mall.recommend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.mall.recommend", "com.mall.common"})
@EnableDiscoveryClient
@EnableScheduling
@MapperScan("com.mall.recommend.mapper")
public class RecommendApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecommendApplication.class, args);
    }
}
