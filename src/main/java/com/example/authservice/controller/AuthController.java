package com.example.authservice.controller;

import com.example.authservice.config.JwtTokenProvider;
import com.example.authservice.model.User;
import com.example.authservice.service.UserService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;
import java.util.Base64;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Auth API", description = "API для регистрации и авторизации")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final Map<String, String> stateStore = new HashMap<>();
    private final PasswordEncoder passwordEncoder;


    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
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
                .filter(u -> passwordEncoder.matches(user.getPassword(), u.getPassword()))
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

    @RestController
    public class OAuth2LoginController {

        @GetMapping("/dashboard")
        public String getDashboard(@AuthenticationPrincipal OAuth2User user) {
            return "Добро пожаловать, " + user.getAttribute("name");
        }
    }

    @GetMapping("/oauth2/authorize/yandex")
    @Operation(summary = "Инициализация OAuth с Яндекс")
    public ResponseEntity<Map<String, String>> initiateYandexOAuth() {

        String state = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
        System.out.println("Сгенерированное state (до сохранения): " + state);
        stateStore.put(state, "valid"); // Сохраняем state в памяти
        Map<String, String> response = new HashMap<>();
        response.put("state", state);

        System.out.println("Инициализация OAuth с Яндекс, stateStore: " + stateStore);

        return ResponseEntity.ok(response);
    }

//    @GetMapping("/login/oauth2/code/yandex")
//    @Operation(summary = "Обработка редиректа от Яндекса")
//    public ResponseEntity<String> handleYandexCallback(
//            @RequestParam("code") String code,
//            @RequestParam("state") String state) {
//        if (!stateStore.containsKey(state)) {
//            return ResponseEntity.badRequest().body("Invalid state parameter");
//        }
//        System.out.println("Обработка редиректа от Яндекса ");
//        System.out.println("code "+code);
//        System.out.println("state "+state);
//
//        // Удаляем state, чтобы избежать повторного использования
//        stateStore.remove(state);
//
//        // Используйте полученный `code` для запроса accessToken и данных пользователя
//        return ResponseEntity.ok("Callback обработан успешно");
//    }

    @GetMapping("/login/oauth2/code/yandex")
    @Operation(summary = "Обработка редиректа от Яндекса")
    public ResponseEntity<String> handleYandexCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state) {
        if (!stateStore.containsKey(state)) {
            return ResponseEntity.badRequest().body("Invalid state parameter");
        }

        // Удаляем state, чтобы избежать повторного использования
        stateStore.remove(state);

        // Отправляем запрос на получение accessToken
        RestTemplate restTemplate = new RestTemplate();
        String tokenUri = "https://oauth.yandex.ru/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("client_id", "a0bc7b7381a84739be01111f12d9447e");
        body.add("client_secret", "c0701b6fad07403c8a8b6f9e99874e1f");
        body.add("redirect_uri", "http://localhost:8081/login/oauth2/code/yandex");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        System.out.println("Запрос на Яндекс: "+request);

        ResponseEntity<String> response = restTemplate.postForEntity(tokenUri, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok("Токен успешно получен: " + response.getBody());
        } else {
            return ResponseEntity.status(response.getStatusCode()).body("Ошибка получения токена");
        }
    }
}

