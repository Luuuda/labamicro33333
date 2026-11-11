package org.example.reviews.application;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewDto(UUID id, UUID restaurantId, String authorName, int rating, String comment, LocalDateTime createdAt) {
}