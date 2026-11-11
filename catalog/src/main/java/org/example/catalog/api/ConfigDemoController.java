package org.example.catalog.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/catalog/config")
@RefreshScope
public class ConfigDemoController {


    @Value("${catalog.greeting:Привіт за замовчуванням}")
    private String greeting;

    @Value("${catalog.feature-flag-new-search:false}")
    private boolean newSearchEnabled;

    @Value("${info.app.environment:unknown}")
    private String environment;

    @Value("${APP_VERSION:dev}")
    private String version;

    @GetMapping
    public Map<String, Object> showConfig() {
        String hostname = System.getenv().getOrDefault("HOSTNAME", "unknown");
        return Map.of(
                "version", version,
                "hostname", hostname,
                "greeting", greeting,
                "newSearchEnabled", newSearchEnabled,
                "environment", environment
        );
    }
}