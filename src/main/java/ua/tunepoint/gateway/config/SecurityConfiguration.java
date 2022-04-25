package ua.tunepoint.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.pattern.PathPatternParser;
import ua.tunepoint.gateway.security.UserContextAuthorizationFilter;
import ua.tunepoint.gateway.service.AuthService;
import ua.tunepoint.security.BaseUserEncoder;

@Configuration
public class SecurityConfiguration {

    @Bean
    public UserContextAuthorizationFilter userContextAuthorizationFilter(AuthService authService) {
        return new UserContextAuthorizationFilter(
                authService,
                new BaseUserEncoder(),
                new PathPatternParser().parse("/auth/**")
        );
    }
}
