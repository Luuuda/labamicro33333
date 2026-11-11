package org.example.users.infrastructure;

import org.example.users.domain.User;
import org.example.users.domain.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<UUID, User> database = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        database.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(database.get(id));
    }
}