package org.example.users.application;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserDto(
        @NotBlank(message = "Ім'я користувача не може бути порожнім")
        String username,

        @NotBlank(message = "Email не може бути порожнім")
        @Email(message = "Невірний формат email")
        String email
) {}