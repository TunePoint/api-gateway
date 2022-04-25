package ua.tunepoint.gateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class MainConfiguration {

    @Bean
    @LoadBalanced
    public WebClient authWebClient() {
        return WebClient.builder()
                .baseUrl(
                        "http://localhost:8080"
                ).build();
    }
}
