package ua.tunepoint.gateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static ua.tunepoint.security.UserContextHeaders.REQUEST_CORRELATION_ID;

@Component
public class RequestCorrelationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(
                exchange.mutate()
                        .request(request -> {
                            request.header(
                                    REQUEST_CORRELATION_ID,
                                    UUID.randomUUID().toString()
                            );
                        }).build()
        );
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 1;
    }
}
