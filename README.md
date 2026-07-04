# My Market App

### Реактивное веб-приложение интернет-магазина, разработанное на Spring Boot 3 и Spring WebFlux

## 📦 Технологии

### Backend
- Java 21
- Spring Boot 3.3.8
- Spring WebFlux (Reactive)
- Spring Data R2DBC (Reactive PostgreSQL)
- Spring Data Redis (Reactive)
- Spring Session (Reactive)
- Spring Cache (Redis)

### Базы данных и кеширование
- PostgreSQL (основная БД)
- Redis (кеширование товаров + хранение корзины)
- Liquibase (миграции БД)

### DevOps
- Docker & Docker Compose
- Maven (Multi-module)
- OpenAPI 3.0 (генерация клиента/сервера)

### Тестирование
- JUnit 5
- Mockito
- Testcontainers
- Reactor Test
- Spring Boot Test

### Архитектура
- Controller-Service-Repository
- Reactive Streams
- Event-driven (асинхронная обработка)
- Microservices (платежный сервис)

## 📁 Структура мультипроекта
my-market-app-skeleton-react/
├── shop/ # Основное веб-приложение магазина
│ ├── src/
│ │ ├── main/
│ │ │ ├── java/ru.yandex.shop/
│ │ │ │ ├── config/ # Конфигурации (Redis, Cache, WebClient)
│ │ │ │ ├── controller/ # REST и Web контроллеры
│ │ │ │ ├── service/ # Бизнес-логика
│ │ │ │ ├── repository/ # R2DBC репозитории
│ │ │ │ ├── model/ # JPA/Entity модели
│ │ │ │ ├── dto/ # Data Transfer Objects
│ │ │ │ ├── mapper/ # MapStruct мапперы
│ │ │ │ ├── exception/ # Обработка ошибок
│ │ │ │ └── enums/ # Перечисления
│ │ │ └── resources/
│ │ │ ├── application.yml
│ │ │ ├── db/changelog/ # Liquibase миграции
│ │ │ └── templates/ # Thymeleaf шаблоны
│ │ └── test/
│ │ ├── java/ru.yandex.shop/
│ │ │ ├── service/ # Unit тесты
│ │ │ ├── it/ # Интеграционные тесты
│ │ │ └── config/ # Тестовые конфигурации
│ │ └── resources/
│ │ └── application-test.yml
│ └── pom.xml
│
├── payment-service/ # RESTful сервис платежей
│ ├── src/
│ │ ├── main/
│ │ │ ├── java/ru.yandex.shop.payment/
│ │ │ │ ├── controller/ # REST контроллеры
│ │ │ │ ├── service/ # Бизнес-логика
│ │ │ │ └── exception/ # Обработка ошибок
│ │ │ └── resources/
│ │ │ ├── application.yml
│ │ │ └── openapi/ # OpenAPI спецификация
│ │ └── test/
│ │ └── java/ru.yandex.shop.payment/
│ │ ├── service/ # Unit тесты
│ │ └── it/ # Интеграционные тесты
│ └── pom.xml
│
├── docker-compose.yml # Запуск всех сервисов
├── pom.xml # Родительский POM
└── README.md

text

## 🚀 Запуск проекта

### Требования
- Java 21
- Docker & Docker Compose
- Maven 3.8+

### 1. Склонировать репозиторий

```bash
git clone https://github.com/WoldemarK/my-market-app-skeleton-react.git
cd my-market-app-skeleton-react
2. Собрать мультипроект
bash
# Собрать все модули
mvn clean install

# Или собрать без тестов
mvn clean install -DskipTests
3. Запуск через Docker Compose (рекомендуемый способ)
bash
# Запустить все сервисы
docker-compose up -d

# Проверить статус
docker-compose ps

# Просмотреть логи
docker-compose logs -f
Приложение будет доступно:

Магазин: http://localhost:8080

Сервис платежей: http://localhost:8081

Swagger UI (магазин): http://localhost:8080/swagger-ui.html

Swagger UI (платежи): http://localhost:8081/swagger-ui.html

4. Локальный запуск (без Docker)
4.1 Запустить PostgreSQL и Redis
bash
# PostgreSQL
docker run -d -p 5432:5432 \
  -e POSTGRES_DB=shop \
  -e POSTGRES_USER=shop \
  -e POSTGRES_PASSWORD=shop \
  --name postgres-shop \
  postgres:15

# Redis
docker run -d -p 6379:6379 \
  --name redis-shop \
  redis:7-alpine
4.2 Запустить сервис платежей
bash
cd payment-service
mvn spring-boot:run -Dspring-boot.run.profiles=local
4.3 Запустить основное приложение
bash
cd shop
mvn spring-boot:run -Dspring-boot.run.profiles=local
🧪 Тестирование
Запуск всех тестов
bash
# Все тесты всех модулей
mvn test

# Только тесты магазина
cd shop && mvn test

# Только тесты платежного сервиса
cd payment-service && mvn test

# Интеграционные тесты
mvn test -Dtest=*IT

# Unit тесты
mvn test -Dtest=*Test
Покрытие тестами
bash
# Сгенерировать отчет о покрытии
mvn jacoco:report

# Открыть отчет
open target/site/jacoco/index.html
📡 API Эндпоинты
Основное приложение (магазин)
Метод	Эндпоинт	Описание
GET	/items	Список товаров (с пагинацией и фильтрацией)
GET	/items/{id}	Товар по ID
GET	/cart/items	Корзина пользователя
POST	/cart/items	Управление корзиной (PLUS/MINUS/DELETE)
POST	/orders	Создание заказа
GET	/orders	Список заказов
GET	/orders/{id}	Заказ по ID
POST	/{id}/upload-image	Загрузка изображения товара
Сервис платежей
Метод	Эндпоинт	Описание
GET	/api/payment/balance	Получение баланса
POST	/api/payment/pay	Осуществление платежа
🗄️ Кеширование в Redis
Структура кеша
text
items:                    # Кеш отдельных товаров
  key: item:{id}
  value: ItemDto JSON

items-page:               # Кеш страниц товаров
  key: {search}-{sort}-{page}-{size}
  value: PageResponse JSON

cart:{sessionId}:         # Корзина пользователя
  field: {itemId}
  value: {count}
Время жизни кеша
Товары: 5 минут

Страницы товаров: 5 минут

Корзина: до очистки

🐳 Docker
Сборка Docker-образов
bash
# Собрать все образы
docker-compose build

# Собрать только магазин
docker build -t shop-app ./shop

# Собрать только платежный сервис
docker build -t payment-service ./payment-service
Запуск отдельных контейнеров
bash
# Только магазин с зависимостями
docker-compose up -d postgres redis shop-app

# Только платежный сервис
docker-compose up -d postgres payment-service
Остановка и очистка
bash
# Остановить все контейнеры
docker-compose down

# Остановить и удалить volumes
docker-compose down -v

# Очистить кеш Redis
docker exec -it redis-shop redis-cli FLUSHALL
⚙️ Конфигурация
Основные свойства (application.yml)
yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/shop
    username: shop
    password: shop
  
  data:
    redis:
      host: localhost
      port: 6379
  
  cache:
    type: redis
    redis:
      time-to-live: 300000  # 5 минут

payment:
  service:
    url: http://localhost:8081

payment-service:
  url: http://localhost:8081
Переменные окружения (Docker)
bash
SPRING_R2DBC_URL=r2dbc:postgresql://postgres:5432/shop
SPRING_R2DBC_USERNAME=
SPRING_R2DBC_PASSWORD=
SPRING_DATA_REDIS_HOST=redis
PAYMENT_SERVICE_URL=http://payment-service:8081
PAYMENT_BALANCE=1000.00
🔧 Возможные проблемы и решения
1. Redis не запущен
bash
docker-compose up -d redis
# или
docker run -d -p 6379:6379 redis:7-alpine
2. Сервис платежей недоступен
bash
# Проверить статус
docker-compose ps payment-service

# Проверить логи
docker-compose logs payment-service
3. Ошибка подключения к PostgreSQL
bash
# Проверить статус
docker-compose ps postgres

# Сбросить БД
docker-compose down -v
docker-compose up -d postgres
📊 Метрики и мониторинг
Actuator эндпоинты
text
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
GET /actuator/cache
📝 OpenAPI спецификация
Магазин
text
http://localhost:8080/v3/api-docs
http://localhost:8080/swagger-ui.html
Платежный сервис
text
http://localhost:8081/v3/api-docs
http://localhost:8081/swagger-ui.html

👨‍💻 Автор
Kovtunov Vladimir