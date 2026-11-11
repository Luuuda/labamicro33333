package org.example.gateway.application;

import java.util.List;
import java.util.UUID;

public record RestaurantDetailsResponse(
        UUID restaurantId,
        RestaurantView restaurant,
        List<ReviewView> reviews,
        boolean partialData,
        List<String> unavailableServices
) {
}
