package com.example.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Настройка CORS
                .csrf(csrf -> csrf.disable()) // Отключение CSRF
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/**").permitAll() // Разрешить доступ к эндпоинтам авторизации
                        .anyRequest().authenticated() // Все остальные запросы требуют аутентификации
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/dashboard")
                        .failureUrl("/auth?error")
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(oidcLogoutSuccessHandler(null))
                );


        return http.build();
    }


    @Bean
    public OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
        return new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:3000"); // Указываем разрешённый источник
        config.addAllowedMethod("*"); // Разрешаем все HTTP-методы
        config.addAllowedHeader("*"); // Разрешаем все заголовки
        config.setAllowCredentials(true); // Разрешаем использование cookie
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }

    private CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:3000"); // Указываем разрешённый источник
        config.addAllowedMethod("*"); // Разрешаем все HTTP-методы
        config.addAllowedHeader("*"); // Разрешаем все заголовки
        config.setAllowCredentials(true); // Разрешаем использование cookie
        source.registerCorsConfiguration("/**", config);
        return null;
    }


}


