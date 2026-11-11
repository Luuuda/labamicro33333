package org.example.gateway.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Component
@RefreshScope
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final long capacity;
    private final long refillTokens;
    private final long refillPeriodMinutes;

    public RateLimitFilter(@Value("${ratelimit.capacity:100}") long capacity,
                           @Value("${ratelimit.refill-tokens:100}") long refillTokens,
                           @Value("${ratelimit.refill-period-minutes:1}") long refillPeriodMinutes) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillPeriodMinutes = refillPeriodMinutes;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/actuator/")) {
            return chain.filter(exchange);
        }

        String key = resolveKey(exchange);
        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());

        if (bucket.tryConsume(1)) {
            return chain.filter(exchange);
        }

        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        byte[] body = "{\"error\":\"Rate limit exceeded. Try again later.\"}".getBytes();
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body))
        );
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(refillTokens, Duration.ofMinutes(refillPeriodMinutes))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveKey(ServerWebExchange exchange) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userId != null) return "user:" + userId;
        String ip = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
        return "ip:" + ip;
    }

    @Override
    public int getOrder() {
        return -90; // Після JwtAuthFilter (-100), щоб мати X-User-Id
    }
}