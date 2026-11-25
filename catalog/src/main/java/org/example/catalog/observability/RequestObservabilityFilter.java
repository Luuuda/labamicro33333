package org.example.catalog.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ЛР7. Для кожного запиту:
 *  - пише в stdout один JSON-рядок (level, method, route, status, duration_ms, version) — для Loki;
 *  - з імовірністю FAIL_RATE повертає 500 (імітація поганої версії для аналізу Argo Rollouts).
 * Метрики (http_server_requests_seconds_*) рахує Spring Boot Actuator + Micrometer.
 * Службові запити /actuator/** (проби, скрейп Prometheus) не чіпаються.
 */
@Component
public class RequestObservabilityFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger("http");

    @Value("${FAIL_RATE:0}")
    private double failRate;

    @Value("${APP_VERSION:dev}")
    private String version;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long start = System.nanoTime();
        try {
            if (failRate > 0 && ThreadLocalRandom.current().nextDouble() < failRate) {
                response.setStatus(500);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"injected failure\"}");
                return;
            }
            chain.doFilter(request, response);
        } finally {
            double ms = (System.nanoTime() - start) / 1_000_000.0;
            Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String route = pattern != null ? pattern.toString() : "unknown"; // шаблон маршруту, не URL
            int status = response.getStatus();
            LOG.info(String.format(java.util.Locale.ROOT,
                    "{\"level\":\"%s\",\"msg\":\"request\",\"method\":\"%s\",\"route\":\"%s\","
                            + "\"status\":%d,\"duration_ms\":%.1f,\"version\":\"%s\"}",
                    status >= 500 ? "error" : "info", request.getMethod(), route, status, ms, version));
        }
    }
}
