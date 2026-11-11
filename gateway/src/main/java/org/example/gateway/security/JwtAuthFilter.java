package org.example.gateway.security;

import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Дозволяємо без авторизації: логін, актуатор
        if (path.startsWith("/auth/") || path.startsWith("/actuator/") || path.startsWith("/admin/")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtService.parseToken(token);
            String userId = claims.getSubject();

            // Безпечне витягування ролей як List<String>
            Object rolesObj = claims.get("roles");
            List<String> roles = new ArrayList<>();
            if (rolesObj instanceof List<?> rawList) {
                for (Object item : rawList) {
                    roles.add(String.valueOf(item));
                }
            }

            // Передаємо контекст безпеки далі мікросервісам через заголовки
            ServerHttpRequest mutated = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Roles", String.join(",", roles))
                    .build();

            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (Exception e) {
            return unauthorized(exchange, "Invalid or expired token: " + e.getMessage());
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        byte[] body = ("{\"error\":\"" + message + "\"}").getBytes();
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body))
        );
    }

    @Override
    public int getOrder() {
        return -100;
    }
}