package ua.tunepoint.gateway.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import reactor.core.publisher.Mono;
import ua.tunepoint.gateway.service.AuthService;
import ua.tunepoint.security.UserEncoder;

import java.util.Optional;

import static ua.tunepoint.security.UserContextHeaders.USER_CONTEXT;

@Slf4j
@RequiredArgsConstructor
public class UserContextAuthorizationFilter implements GlobalFilter, Ordered {

    private final AuthService authService;
    private final UserEncoder userEncoder;
    private final PathPattern ignore;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Processing request...");

        if (ignore.matches(PathContainer.parsePath(exchange.getRequest().getURI().toString()))) {
            return chain.filter(exchange);
        }

        final var bearerHolder = extractBearer(exchange.getRequest());

        if (bearerHolder.isPresent()) {
            var bearer = bearerHolder.get();
            var userMono = authService.authenticateUser(bearer);

            return userMono.flatMap(user -> {
                var newExchange = exchange.mutate()
                        .request(request -> {
                            request.header(
                                    USER_CONTEXT,
                                    userEncoder.encode(user)
                            );
                        }).build();

                return chain.filter(newExchange);

            }).onErrorResume(t -> {
                log.error("Error occurred while authenticating user", t);

                var errorResponse = exchange.getResponse();
                errorResponse.setRawStatusCode(HttpStatus.UNAUTHORIZED.value());

                return errorResponse.setComplete();
            });
        }

        return chain.filter(exchange);
    }

    private Optional<String> extractBearer(ServerHttpRequest request) {
        var authHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (authHeaders == null || authHeaders.size() != 1) {
            return Optional.empty();
        }
        return Optional.ofNullable(authHeaders.get(0));
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
