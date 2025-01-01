# AuthService

## Описание
**AuthService** — это универсальный микросервис авторизации, реализующий OAuth2 с использованием JWT. Подходит для регистрации и авторизации пользователей, с поддержкой генерации access- и refresh-токенов.

## Возможности
- Регистрация пользователей.
- Авторизация с генерацией токенов.
- Возможность интеграции с другими микросервисами.

## Требования
- **Java 17**
- **Spring Boot 3**
- **Gradle**
- **H2 Database** (или другая база данных)

## Установка и запуск

1. Склонируйте репозиторий:
   
   git clone https://github.com/sergiologino/auth-service.git
   
   cd auth-service
   
3. Настройте конфигурацию в application.yml (по необходимости):

   jwt:
   secret: your-secret-key
   expiration:
   access: 3600000  # 1 час
   refresh: 86400000  # 1 день

4. Сборка и запуск:

   ./gradlew bootRun

5. Откройте Swagger для тестирования API:
   URL: http://localhost:8081/swagger-ui.html

API Документация

Регистрация пользователя

POST /api/auth/register

Пример запроса:

{
"username": "testuser",
"password": "testpass",
"email": "test@example.com"
}
Пример ответа:
{
"message": "User registered successfully"
}

Авторизация пользователя
POST /api/auth/login

Пример запроса:
{
"username": "testuser",
"password": "testpass"
}

Пример ответа:
{
"accessToken": "eyJhbGciOiJIUzI1NiIsInR...",
"refreshToken": "eyJhbGciOiJIUzI1NiIsInR..."
}
Разработчики
Sergey Savkin
