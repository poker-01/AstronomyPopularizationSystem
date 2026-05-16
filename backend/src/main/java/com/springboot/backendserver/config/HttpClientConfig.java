package com.springboot.backendserver.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {

    private static final int NASA_CONNECT_TIMEOUT_MS = 3000;
    private static final int NASA_READ_TIMEOUT_MS = 4000;
    private static final int AI_CONNECT_TIMEOUT_MS = 10000;
    private static final int AI_READ_TIMEOUT_MS = 120000;

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder().requestFactory(requestFactory(NASA_CONNECT_TIMEOUT_MS, NASA_READ_TIMEOUT_MS));
    }

    @Bean
    @Qualifier("deepSeekRestClientBuilder")
    public RestClient.Builder deepSeekRestClientBuilder() {
        return RestClient.builder().requestFactory(requestFactory(AI_CONNECT_TIMEOUT_MS, AI_READ_TIMEOUT_MS));
    }

    private static SimpleClientHttpRequestFactory requestFactory(int connectMs, int readMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectMs);
        factory.setReadTimeout(readMs);
        return factory;
    }
}
