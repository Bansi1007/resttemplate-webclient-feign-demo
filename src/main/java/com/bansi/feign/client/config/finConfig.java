package com.bansi.feign.client.config;

import feign.okhttp.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class finConfig {
    @Bean
    public OkHttpClient client() {
        return new OkHttpClient();
    }
}
