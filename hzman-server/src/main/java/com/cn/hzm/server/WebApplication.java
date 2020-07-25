package com.cn.hzm.server;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.cn.hzm")
public class WebApplication {

    public static void main(String [] args) {
        new SpringApplicationBuilder(WebApplication.class).web(WebApplicationType.SERVLET).build().run(args);
    }

}
