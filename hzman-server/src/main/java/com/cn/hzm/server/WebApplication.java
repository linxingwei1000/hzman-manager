package com.cn.hzm.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.cn.hzm")
public class WebApplication {

    public static void main(String [] args) {
        SpringApplication.run(WebApplication.class, args);
    }

}
