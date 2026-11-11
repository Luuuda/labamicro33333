package org.example.gateway.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RefreshScope
public class AdminController {

    @Value("${admin.api-key:not-set}")
    private String apiKey;

    @Value("${admin.recovery-token:not-set}")
    private String recoveryToken;

    @GetMapping("/secrets-info")
    public Map<String, Object> info() {
        return Map.of(
                "apiKeyLength", apiKey.length(),
                "apiKeyMasked", mask(apiKey),
                "recoveryTokenMasked", mask(recoveryToken),
                "note", "Реальні значення не показуються — лише довжина і маска"
        );
    }

    private String mask(String value) {
        if (value == null || value.length() < 4) return "****";
        return value.substring(0, 3) + "****" + value.substring(value.length() - 2);
    }
}