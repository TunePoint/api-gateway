package ua.tunepoint.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class MainConfiguration {

    @Value("${auth.service-id}")
    private String authServiceId;

    @Bean
    @LoadBalanced
    public WebClient.Builder authWebClient() {
        return WebClient
                .builder()
                .baseUrl("http://" + authServiceId);
    }
}
