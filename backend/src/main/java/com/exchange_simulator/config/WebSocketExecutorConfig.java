package com.exchange_simulator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class WebSocketExecutorConfig {

    @Bean
    public Executor socketCallbackExecutor(){
        return Executors.newFixedThreadPool(4);
    }
}
