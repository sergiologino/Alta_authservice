package com.example.authservice.controller;

import com.example.authservice.config.JwtTokenProvider;
import com.example.authservice.model.User;
import com.example.authservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth API", description = "API для регистрации и авторизации")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя", description = "Регистрация нового пользователя в системе")
    public ResponseEntity<String> register(@RequestBody User user) {
        userService.registerUser(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    @Operation(summary = "Авторизация пользователя", description = "Авторизация пользователя и выдача токенов")
    public ResponseEntity<Map<String, String>> login(@RequestBody User user) {
        return userService.findByUsername(user.getUsername())
                .filter(u -> jwtTokenProvider.validateToken(u.getPassword()))
                .map(u -> {
                    String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername());
                    String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

                    Map<String, String> tokens = new HashMap<>();
                    tokens.put("accessToken", accessToken);
                    tokens.put("refreshToken", refreshToken);
                    return ResponseEntity.ok(tokens);
                })
                .orElse(ResponseEntity.status(401).build());
    }
}
