package org.example.gateway.application;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewView(
        UUID id,
        UUID restaurantId,
        String authorName,
        int rating,
        String comment,
        String createdAt
) {
}
