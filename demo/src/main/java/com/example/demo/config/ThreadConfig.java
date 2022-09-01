package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class ThreadConfig {
    @Bean("heartBeatExecutor")
    public Executor heartBeatThreadExecutor()
    {
        ThreadPoolTaskExecutor threadPoolTaskExecutor=new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);//核心线程数量
        threadPoolTaskExecutor.setMaxPoolSize(10);//最大线程数量
        threadPoolTaskExecutor.setKeepAliveSeconds(60);//线程闲置的时候存活的时间
        threadPoolTaskExecutor.setThreadNamePrefix("");
        threadPoolTaskExecutor.initialize();
        return  threadPoolTaskExecutor;
    }
}
