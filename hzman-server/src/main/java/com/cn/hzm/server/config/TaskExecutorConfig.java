package com.cn.hzm.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/21 4:10 下午
 */
@Configuration
public class TaskExecutorConfig {

    @Bean(name = "backTaskExecutor")
    public ThreadPoolTaskExecutor searchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(20);
        executor.setThreadNamePrefix("backTaskExecutor");
        executor.setQueueCapacity(10);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("processItemInfoThreadExecutor")
    public ExecutorService processItemInfoThreadExecutor() {
        // 等待队列
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(1000);
        return new ThreadPoolExecutor(30, 30, 60L, TimeUnit.SECONDS,
                workQueue, r -> new Thread(r, "process-item-thread"));
    }
}
