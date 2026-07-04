Payment Service
RESTful сервис платежей для интернет-магазина
📦 Технологии
Java 21

Spring Boot 3.3.8

Spring WebFlux (Reactive)

OpenAPI 3.0 (Code Generation)

Lombok

JUnit 5 / Mockito / Reactor Test

Docker

🛠 Технологии
Технология	Версия	Описание
Java	21	Основной язык программирования
Spring Boot	3.3.8	Фреймворк приложения
Spring WebFlux	3.3.8	Реактивный веб-фреймворк
OpenAPI Generator	7.3.0	Генерация кода по OpenAPI спецификации
Lombok	1.18.36	Упрощение кода
JUnit 5	5.10.5	Тестирование
Mockito	5.11.0	Мокирование для тестов
Reactor Test	3.6.13	Тестирование реактивных потоков
Docker	-	Контейнеризация

⚡ Функциональность
Основные возможности
Управление балансом

Получение текущего баланса

Настройка начального баланса через конфигурацию

Обработка платежей

Списание средств с баланса

Проверка достаточности средств

Возврат ошибок при недостатке баланса

Валидация запросов

Проверка суммы платежа (должна быть положительной)

Проверка формата запроса

Логирование

Логирование всех операций

Трассировка запросов

Мониторинг

Actuator эндпоинты

Метрики

Health checks

🚀 Запуск
Требования
Java 21

Maven 3.8+

Docker (опционально)

Локальный запуск
bash
# Перейти в директорию сервиса
cd payment-service

# Собрать проект
mvn clean package

# Запустить приложение
java -jar target/payment-service-1.0.0-SNAPSHOT.jar

# Или через Maven (для разработки)
mvn spring-boot:run

# С определенным профилем
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# С настройкой начального баланса
java -jar target/payment-service-1.0.0-SNAPSHOT.jar --payment.balance=5000.00
Запуск с переменными окружения
bash
export PAYMENT_BALANCE=5000.00
export SERVER_PORT=8082
java -jar target/payment-service-*.jar
Через Docker
bash
# Собрать образ
docker build -t payment-service:latest .

# Запустить контейнер
docker run -d \
-p 8081:8081 \
-e PAYMENT_BALANCE=1000.00 \
--name payment-service \
payment-service:latest

# Проверить логи
docker logs -f payment-service
Через Docker Compose (совместно с магазином)
bash
# Из корневой директории проекта
docker-compose up -d payment-service

# Запустить все сервисы
docker-compose up -d

# Проверить статус
docker-compose ps

# Остановить
docker-compose down
📡 API Эндпоинты
Базовый URL
text
http://localhost:8081/api/payment
1. Получение баланса
   GET /api/payment/balance

Получить текущий баланс на счете.

Успешный ответ (200 OK)
json
{
"balance": 1000.00
}
Примеры запросов
bash
# curl
curl -X GET http://localhost:8081/api/payment/balance

# HTTP
GET /api/payment/balance HTTP/1.1
Host: localhost:8081

# Java WebClient
webClient.get()
.uri("/api/payment/balance")
.retrieve()
.bodyToMono(BalanceResponse.class)
2. Осуществление платежа
   POST /api/payment/pay

Списать сумму с баланса.

Запрос
json
{
"amount": 150.50
}
Успешный ответ (200 OK)
json
{
"success": true,
"message": "Payment successful"
}
Ошибка при недостатке средств (402 Payment Required)
json
{
"code": "INSUFFICIENT_BALANCE",
"message": "Not enough balance. Current: 100.00, Required: 150.50"
}
Ошибка при неверном запросе (400 Bad Request)
json
{
"code": "INVALID_REQUEST",
"message": "Amount must be positive"
}
Примеры запросов
bash
# curl
curl -X POST http://localhost:8081/api/payment/pay \
-H "Content-Type: application/json" \
-d '{"amount": 150.50}'

# HTTP
POST /api/payment/pay HTTP/1.1
Host: localhost:8081
Content-Type: application/json

{
"amount": 150.50
}

# Java WebClient
PaymentRequest request = new PaymentRequest();
request.setAmount(BigDecimal.valueOf(150.50));

webClient.post()
.uri("/api/payment/pay")
.bodyValue(request)
.retrieve()
.bodyToMono(PaymentResponse.class)
3. Health Check
   GET /actuator/health

Проверка работоспособности сервиса.

Ответ (200 OK)
json
{
"status": "UP"
}
📁 Структура проекта
text
payment-service/
├── src/
│   ├── main/
│   │   ├── java/ru/yandex/shop/payment/
│   │   │   ├── PaymentServiceApplication.java      # Точка входа
│   │   │   ├── controller/
│   │   │   │   └── PaymentController.java          # REST контроллер
│   │   │   ├── service/
│   │   │   │   └── PaymentService.java             # Бизнес-логика
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java     # Обработка ошибок
│   │   │       └── InsufficientBalanceException.java # Кастомное исключение
│   │   └── resources/
│   │       ├── application.yml                      # Конфигурация
│   │       └── openapi/
│   │           └── payment-api.yaml                 # OpenAPI спецификация
│   └── test/
│       └── java/ru/yandex/shop/payment/
│           ├── service/
│           │   └── PaymentServiceTest.java          # Unit тесты
│           └── it/
│               └── PaymentControllerIT.java         # Интеграционные тесты
├── Dockerfile                                        # Docker сборка
└── pom.xml                                           # Maven конфигурация
Описание ключевых компонентов
Компонент	Описание
PaymentController	Обрабатывает HTTP запросы, делегирует логику в сервис
PaymentService	Содержит бизнес-логику: проверка баланса, списание средств
GlobalExceptionHandler	Глобальная обработка исключений, формирование ошибок
PaymentServiceApplication	Главный класс приложения с настройкой конфигураций
⚙️ Конфигурация
application.yml
yaml
spring:
application:
name: payment-service

# Настройка баланса
payment:
balance: 1000.00  # Начальный баланс

# Сервер
server:
port: 8081

# Логирование
logging:
level:
ru.yandex.shop.payment: DEBUG
org.springframework.web: INFO
reactor.netty: INFO

# Actuator мониторинг
management:
endpoints:
web:
exposure:
include: health,info,metrics
endpoint:
health:
show-details: always
metrics:
export:
simple:
enabled: true
Профили
application-dev.yml
yaml
payment:
balance: 5000.00

logging:
level:
ru.yandex.shop.payment: DEBUG
application-prod.yml
yaml
payment:
balance: 10000.00

server:
port: 8080

logging:
level:
ru.yandex.shop.payment: INFO
Переменные окружения
Переменная	Описание	Значение по умолчанию
PAYMENT_BALANCE	Начальный баланс	1000.00
SERVER_PORT	Порт сервиса	8081
SPRING_PROFILES_ACTIVE	Активный профиль	default
LOGGING_LEVEL_RU_YANDEX_SHOP_PAYMENT	Уровень логирования	INFO
🧪 Тестирование
Запуск тестов
bash
# Все тесты
mvn test

# Unit тесты
mvn test -Dtest=*Test

# Интеграционные тесты
mvn test -Dtest=*IT

# С покрытием
mvn test jacoco:report

# С конкретным классом
mvn test -Dtest=PaymentServiceTest
Примеры тестов
Unit тест PaymentService
java
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("Успешный платеж")
    void shouldMakePaymentSuccessfully() {
        // Given
        BigDecimal initialBalance = BigDecimal.valueOf(1000);
        paymentService.setBalance(initialBalance);
        PaymentRequest request = new PaymentRequest();
        request.setAmount(BigDecimal.valueOf(150.50));

        // When
        PaymentResponse response = paymentService.makePayment(request);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Payment successful");
        assertThat(paymentService.getBalance()).isEqualTo(BigDecimal.valueOf(849.50));
    }

    @Test
    @DisplayName("Ошибка при недостатке средств")
    void shouldThrowExceptionWhenInsufficientBalance() {
        // Given
        paymentService.setBalance(BigDecimal.valueOf(100));
        PaymentRequest request = new PaymentRequest();
        request.setAmount(BigDecimal.valueOf(150));

        // When / Then
        assertThatThrownBy(() -> paymentService.makePayment(request))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Not enough balance");
    }

    @Test
    @DisplayName("Пополнение баланса")
    void shouldAddBalance() {
        // Given
        paymentService.setBalance(BigDecimal.valueOf(100));
        
        // When
        paymentService.addBalance(BigDecimal.valueOf(50));
        
        // Then
        assertThat(paymentService.getBalance()).isEqualTo(BigDecimal.valueOf(150));
    }
}
Интеграционный тест
java
@SpringBootTest(
webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
properties = {
"payment.balance=1000.00"
}
)
@AutoConfigureWebTestClient
class PaymentControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("GET /api/payment/balance - возвращает баланс")
    void shouldReturnBalance() {
        webTestClient.get()
            .uri("/api/payment/balance")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.balance").isEqualTo(1000.00);
    }

    @Test
    @DisplayName("POST /api/payment/pay - успешный платеж")
    void shouldProcessPaymentSuccessfully() {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(BigDecimal.valueOf(100));

        webTestClient.post()
            .uri("/api/payment/pay")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.message").isEqualTo("Payment successful");
    }

    @Test
    @DisplayName("POST /api/payment/pay - ошибка при недостатке средств")
    void shouldReturnErrorWhenInsufficientBalance() {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(BigDecimal.valueOf(2000)); // > баланса

        webTestClient.post()
            .uri("/api/payment/pay")
            .bodyValue(request)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.PAYMENT_REQUIRED)
            .expectBody()
            .jsonPath("$.code").isEqualTo("INSUFFICIENT_BALANCE");
    }
}
Покрытие тестами
bash
# Сгенерировать отчет
mvn jacoco:report

# Открыть отчет
open target/site/jacoco/index.html
Целевое покрытие:

✅ Классы: 100%

✅ Методы: 100%

✅ Строки: 90%+

🐳 Docker
Dockerfile
dockerfile
# Build stage
FROM maven:3.8.4-openjdk-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=build /app/target/payment-service-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
Сборка и запуск
bash
# Собрать образ
docker build -t payment-service:latest .

# Запустить с настройкой баланса
docker run -d \
-p 8081:8081 \
-e PAYMENT_BALANCE=5000.00 \
--name payment-service \
--restart always \
payment-service:latest

# Просмотр логов
docker logs -f payment-service

# Остановка и удаление
docker stop payment-service
docker rm payment-service
🔗 Интеграция
С основным приложением магазина
1. Генерация клиента через OpenAPI
   В pom.xml основного приложения:

xml
<plugin>
<groupId>org.openapitools</groupId>
<artifactId>openapi-generator-maven-plugin</artifactId>
<version>7.3.0</version>
<executions>
<execution>
<goals>
<goal>generate</goal>
</goals>
<configuration>
<inputSpec>${project.basedir}/src/main/resources/openapi/payment-api.yaml</inputSpec>
<generatorName>java</generatorName>
<apiPackage>ru.shop.payment.api</apiPackage>
<modelPackage>ru.shop.payment.dto</modelPackage>
<invokerPackage>ru.shop.payment</invokerPackage>
<configOptions>
<reactive>true</reactive>
<useSpringBoot3>true</useSpringBoot3>
<useJakartaEe>true</useJakartaEe>
<library>webclient</library>
</configOptions>
</configuration>
</execution>
</executions>
</plugin>
2. Конфигурация клиента
   java
   @Configuration
   public class PaymentClientConfig {

   @Bean
   public WebClient paymentWebClient(
   @Value("${payment-service.url:http://localhost:8081}") String baseUrl,
   WebClient.Builder builder) {
   return builder
   .baseUrl(baseUrl)
   .defaultHeader("Content-Type", "application/json")
   .build();
   }

   @Bean
   public PaymentClient paymentClient(WebClient paymentWebClient) {
   return new PaymentClient(paymentWebClient);
   }
   }
3. Использование в сервисе
   java
   @Service
   @RequiredArgsConstructor
   public class OrderService {
   private final PaymentClient paymentClient;

   public Mono<Long> createOrder(String sessionId) {
   // Получить баланс
   return paymentClient.getBalance()
   .flatMap(balance -> {
   if (balance.getBalance().compareTo(total) < 0) {
   return Mono.error(new InsufficientBalanceException());
   }

                // Осуществить платеж
                PaymentRequest request = new PaymentRequest();
                request.setAmount(total);
                
                return paymentClient.pay(request)
                    .flatMap(response -> {
                        if (!response.getSuccess()) {
                            return Mono.error(new PaymentFailedException());
                        }
                        return saveOrder(sessionId);
                    });
            });
   }
   }
   📝 OpenAPI
   Спецификация (payment-api.yaml)
   yaml
   openapi: 3.0.3
   info:
   title: Payment Service API
   description: RESTful сервис для обработки платежей
   version: 1.0.0
   contact:
   name: Support
   email: support@example.com

servers:
- url: http://localhost:8081/api/payment
  description: Локальный сервер
- url: http://payment-service:8081/api/payment
  description: Docker сеть

paths:
/balance:
get:
summary: Получить баланс
operationId: getBalance
tags:
- Payment
responses:
'200':
description: Успешный ответ
content:
application/json:
schema:
$ref: '#/components/schemas/BalanceResponse'
'500':
description: Внутренняя ошибка

/pay:
post:
summary: Осуществить платеж
operationId: makePayment
tags:
- Payment
requestBody:
required: true
content:
application/json:
schema:
$ref: '#/components/schemas/PaymentRequest'
responses:
'200':
description: Платеж успешен
content:
application/json:
schema:
$ref: '#/components/schemas/PaymentResponse'
'400':
description: Неверный запрос
content:
application/json:
schema:
$ref: '#/components/schemas/ErrorResponse'
'402':
description: Недостаточно средств
content:
application/json:
schema:
$ref: '#/components/schemas/ErrorResponse'

components:
schemas:
BalanceResponse:
type: object
properties:
balance:
type: number
format: double
example: 1000.00
description: Текущий баланс
required:
- balance

    PaymentRequest:
      type: object
      properties:
        amount:
          type: number
          format: double
          example: 150.50
          description: Сумма платежа
          minimum: 0.01
      required:
        - amount
    
    PaymentResponse:
      type: object
      properties:
        success:
          type: boolean
          example: true
          description: Статус платежа
        message:
          type: string
          example: Payment successful
          description: Сообщение о результате
      required:
        - success
    
    ErrorResponse:
      type: object
      properties:
        code:
          type: string
          example: INSUFFICIENT_BALANCE
          description: Код ошибки
        message:
          type: string
          example: Not enough balance
          description: Описание ошибки
      required:
        - code
        - message
Swagger UI
Доступен по адресу:

text
http://localhost:8081/swagger-ui.html
📊 Мониторинг
Actuator эндпоинты
Эндпоинт	Описание
/actuator/health	Статус сервиса
/actuator/info	Информация о приложении
/actuator/metrics	Метрики приложения
/actuator/metrics/balance	Метрика баланса
/actuator/loggers	Управление логированием
Метрики
yaml
management:
metrics:
export:
simple:
enabled: true
tags:
application: payment-service
🔧 Возможные проблемы и решения
1. Сервис не запускается
   Проблема: Порт 8081 уже занят

Решение:

bash
# Найти процесс на порту 8081
sudo lsof -i :8081

# Убить процесс
sudo kill -9 PID

# Или изменить порт
java -jar target/*.jar --server.port=8082
2. Ошибка при платеже
   Проблема: Недостаточно средств

Решение:

bash
# Увеличить баланс
export PAYMENT_BALANCE=5000.00
java -jar target/*.jar

# Или через API пополнить (если реализовано)
3. OpenAPI генерация не работает
   Проблема: Ошибка при генерации клиента

Решение:

bash
# Перегенерировать код
mvn clean generate-sources

# Проверить OpenAPI файл
cat src/main/resources/openapi/payment-api.yaml
4. Интеграционные тесты падают
   Проблема: Не запущен контейнер тестовой БД

Решение:

bash
# Запустить зависимости
docker-compose up -d postgres redis

# Или использовать Testcontainers (автоматически)
mvn test -Dtest=*IT
📚 Документация
Внутренняя документация
Архитектура

API Документация

Гайд по интеграции

Сборка и деплой

Полезные ссылки
Spring WebFlux

Project Reactor

OpenAPI Generator

Docker

👨‍💻 Поддержка
Автор
Kovtunov Vladimir

GitHub: @WoldemarK

Telegram: @K_Waldemar

Версия: 1.0.0
Статус: Production Ready
Дата: 2026


