package org.example.users.application;

import org.example.users.domain.User;
import org.example.users.domain.UserRepository;
import org.springframework.stereotype.Service;


@Service

public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public UserDto registerUser(CreateUserDto dto) {
        User user = new User(null, dto.username(), dto.email());
        User savedUser = repository.save(user);
        return new UserDto(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
    }
}