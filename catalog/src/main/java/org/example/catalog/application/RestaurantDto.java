package org.example.catalog.application;

import java.util.UUID;

public record RestaurantDto(UUID id, String name, String description, String address) {
}