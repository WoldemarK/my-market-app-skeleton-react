# Auth Service

Сервис регистрации пользователей с использованием **Spring Boot** и **Keycloak**.

## Возможности

- регистрация нового пользователя;
- установка пароля;
- автоматическое назначение роли `USER`.

## Стек технологий

- Java 21
- Spring Boot
- Spring Security
- Keycloak Admin Client
- Maven
- Lombok

## Логика регистрации

При вызове метода `register(RegisterRequest request)` выполняются следующие шаги:

1. Получение Realm из Keycloak.
2. Создание пользователя.
3. Проверка успешности создания (HTTP 201).
4. Получение идентификатора созданного пользователя.
5. Установка пароля.
6. Назначение пользователю роли `USER`.

Если пользователь уже существует, сервис выбрасывает:

```java
RuntimeException("User already exists")
```

---

## Структура проекта

```
src
├── controller
├── service
│   └── AuthService
├── dto
│   └── RegisterRequest
├── config
│   └── KeycloakProperties
└── security
```

---

## Конфигурация

Пример `application.yml`

```yaml
keycloak:
  server-url: http://localhost:8080
  realm: myrealm
  client-id: admin-cli
  username: admin
  password: admin
```

---

## Регистрация пользователя

Пример DTO:

```json
{
  "firstname": "Ivan",
  "lastname": "Ivanov",
  "email": "ivan@test.com",
  "password": "password123"
}
```

После успешной регистрации:

- пользователь создаётся в Keycloak;
- пароль сохраняется;
- назначается роль `USER`.

---

## Запуск

```bash
mvn clean install
mvn spring-boot:run
```

или

```bash
docker compose up
```

