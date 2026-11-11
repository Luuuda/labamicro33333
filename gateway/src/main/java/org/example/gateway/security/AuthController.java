package org.example.gateway.security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;

    // Hardcoded users — нормально для лаби, у проді була б БД + bcrypt
    private static final Map<String, UserCreds> USERS = Map.of(
            "user",  new UserCreds("password", List.of("USER")),
            "admin", new UserCreds("password", List.of("USER", "ADMIN"))
    );

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        UserCreds creds = USERS.get(request.username());
        if (creds == null || !creds.password.equals(request.password())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
        String token = jwtService.generateToken(request.username(), creds.roles);
        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", request.username(),
                "roles", creds.roles
        ));
    }

    public record LoginRequest(String username, String password) {}
    private record UserCreds(String password, List<String> roles) {}
}