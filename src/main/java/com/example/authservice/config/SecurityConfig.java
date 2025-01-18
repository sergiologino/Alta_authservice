package com.example.authservice.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Разрешить доступ для стандартных регистрационных запросов
                        .requestMatchers("/oauth2/**").permitAll() // Разрешить доступ для OAuth2

                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/yandex") // Настройка Yandex OAuth2
                        .defaultSuccessUrl("/dashboard")
                        .failureUrl("/auth?error")
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/auth")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:3000/"); // Укажите фронтенд-URL
        config.addAllowedOrigin("http://localhost:8080/"); // Укажите фронтенд-URL
        config.addAllowedMethod("*"); // Разрешите все методы
        config.addAllowedHeader("*"); // Разрешите все заголовки
        config.setAllowCredentials(true); // Включите поддержку cookies
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}


