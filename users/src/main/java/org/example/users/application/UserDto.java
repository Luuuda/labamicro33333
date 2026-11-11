package org.example.users.application;

import java.util.UUID;

public record UserDto(UUID id, String username, String email) {
}