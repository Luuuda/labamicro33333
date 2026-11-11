package org.example.users.api;

import jakarta.validation.Valid;
import org.example.users.application.CreateUserDto;
import org.example.users.application.UserDto;
import org.example.users.application.UserService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public UserDto registerUser(@Valid @RequestBody CreateUserDto dto) {
        return userService.registerUser(dto);

    }
}