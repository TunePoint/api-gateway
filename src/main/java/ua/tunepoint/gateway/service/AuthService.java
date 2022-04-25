package ua.tunepoint.gateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ua.tunepoint.auth.model.response.UserResponse;
import ua.tunepoint.security.UserView;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final WebClient.Builder webClient;

    public Mono<UserView> authenticateUser(String jwtHeader) {
        var responseMono = webClient.build().get()
                .uri("/auth")
                .header(AUTHORIZATION, jwtHeader)
                .retrieve().bodyToMono(UserResponse.class);

        return responseMono.map(userResponse -> {
            var user = userResponse.getPayload();
            return UserView.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .authorities(user.getAuthorities())
                    .build();
        });
    }
}
