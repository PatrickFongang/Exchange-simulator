package com.exchange_simulator.config;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.binance.BinanceExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExchangeConfig {
    @Bean
    public Exchange binanceExchange() {
        return ExchangeFactory.INSTANCE.createExchange(BinanceExchange.class);
    }
}
