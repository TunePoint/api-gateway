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

        final var safeExchange = exchange.mutate()
                .request(request -> {
                    request.headers(
                            httpHeaders -> httpHeaders.remove(USER_CONTEXT)
                    );
                }).build();

        if (ignore.matches(PathContainer.parsePath(safeExchange.getRequest().getURI().toString()))) {
            return chain.filter(safeExchange);
        }

        final var bearerHolder = extractBearer(safeExchange.getRequest());

        if (bearerHolder.isPresent()) {
            var bearer = bearerHolder.get();
            var userMono = authService.authenticateUser(bearer);

            return userMono.flatMap(user -> {
                var newExchange = safeExchange.mutate()
                        .request(request -> {
                            request.header(
                                    USER_CONTEXT,
                                    userEncoder.encode(user)
                            );
                        }).build();

                return chain.filter(newExchange);

            }).onErrorResume(t -> {
                log.error("Error occurred while authenticating user", t);

                var errorResponse = safeExchange.getResponse();
                errorResponse.setRawStatusCode(HttpStatus.UNAUTHORIZED.value());

                return errorResponse.setComplete();
            });
        }

        return chain.filter(safeExchange);
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
