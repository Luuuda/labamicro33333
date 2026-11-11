package org.example.catalog.application;

import jakarta.validation.constraints.NotBlank;

public record CreateRestaurantDto(
        @NotBlank(message = "Назва ресторану є обов'язковою")
        String name,

        String description, // Опис може бути порожнім, тому тут не ставимо перевірку

        @NotBlank(message = "Адреса є обов'язковою")
        String address
) {}