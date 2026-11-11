package org.example.gateway.application;

import java.util.UUID;

public record RestaurantView(
        UUID id,
        String name,
        String description,
        String address
) {
}
