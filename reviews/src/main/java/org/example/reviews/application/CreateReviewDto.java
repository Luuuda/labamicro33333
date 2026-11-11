package org.example.reviews.application;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateReviewDto(
        @NotNull(message = "ID ресторану є обов'язковим")
        UUID restaurantId,

        @NotBlank(message = "Ім'я автора є обов'язковим")
        String authorName,

        @Min(value = 1, message = "Мінімальна оцінка 1")
        @Max(value = 5, message = "Максимальна оцінка 5")
        int rating,

        String comment
) {}