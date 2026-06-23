# My Market App

### Реактивное веб-приложение интернет-магазина, разработанное на Spring Boot 3 и Spring WebFlux

## 📦 Технологии
###  Backend

- Java 21
- Spring WebFlux
- Spring Data R2DBC

- PostgreSQL
- Redis
- Docker
- Reactor
- JUnit 5 / Testcontainers / Mockito

###  DevOps
- Reactor
- Docker Compose
- Docker Compose
- Maven

###  Архитектура
- Controller
- Service
- Repository
- PostgreSQL
- Дополнительно используется Redis для хранения данных корзины и кэширования.


## 🚀 Запуск проекта локально

### 1. Склонировать репозиторий

```bash
git clone git clone https://github.com/WoldemarK/my-market-app-skeleton-react.git
cd my-blog-back-app
```

---

### 2. Собрать проект

```bash
mvn clean package
```

После сборки появится файл:

```bash
target/*.jar
```

---

### 3. Запустить приложение

```bash
java -jar target/*.jar
```

Приложение будет доступно:

```text
http://localhost:8080
```

---

## 🐳 Docker

### Сборка Docker-образа

```bash
docker build -t my-market-app-skeleton-react .
```

---

### Запуск контейнера

```bash
docker run -p 8080:8080 my-market-app-skeleton-react
```

---

## 📂 Структура проекта

```text
src
├── main
│   ├── java
│   │   └── ru.yandex.mymarketappskeleton
│   │       ├── config
│   │       ├── controller   
│   │       ├── enums
│   │       ├── exception
│   │       ├── dto
│   │       ├── model
│   │       ├── repository
│   │       ├── service
│   │       └── mapper
│   └── resources
│        └── application.properties
│        │   └── schema.sql
│        │   └── data.sql
│        └── templates
│        └── db.changelog
│
└── test
├── java
│   └── ru.yandex.mymarketappskeleton
│       ├── controller
│       ├── service
│       └── config
        └── it
```

---

## 🧪 Тесты

Запуск всех тестов:

```bash
mvn test
```

---

## 📡 Основные REST API

### Получить список постов

```http
GET /api/posts
```

---

### Получить пост

```http
GET /api/posts/{id}
```

---

### Создать пост

```http
POST /api/posts
```

---

### Удалить пост

```http
DELETE /api/posts/{id}
```

---

## 📸 Работа с изображениями

### Загрузка изображения

```http
PUT /api/posts/{id}/image
```

### Получение изображения

```http
GET /api/posts/{id}/image
```

---

## 📝 Комментарии

### Получить комментарии

```http
GET /api/posts/{id}/comments
```

### Добавить комментарий

```http
POST /api/posts/{id}/comments
```

---

## ⚙️ Переменные окружения

Пример:

```bash
DB_URL=jdbc:postgresql://localhost:5432/blog
DB_USER=postgres
DB_PASSWORD=postgres
```

---

## 🐳 Dockerfile

Проект использует multi-stage build:

1. Maven собирает jar
2. Финальный образ содержит только Java Runtime

---

## 👨‍💻 Автор

[Kovtunov Vladimir](https://github.com/WoldemarK)
[Software Engineering Telegram](https://t.me/K_Waldemar)