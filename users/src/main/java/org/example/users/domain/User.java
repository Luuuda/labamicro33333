package org.example.users.domain;

import java.util.UUID;

public class User {
    private final UUID id;
    private String username;
    private String email;

    public User(UUID id, String username, String email) {
        this.id = id != null ? id : UUID.randomUUID();
        this.username = username;
        this.email = email;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
}