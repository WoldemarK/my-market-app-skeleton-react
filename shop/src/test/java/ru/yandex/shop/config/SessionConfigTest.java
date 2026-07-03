package ru.yandex.shop.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "payment-service.url=http://localhost:8081",
                "spring.main.allow-bean-definition-overriding=true" // Добавьте это
        }
)
@AutoConfigureWebTestClient
class SessionConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    private final static String SESSION = "sessionId";
    private final static String TEST_URL = "/test";
    private final static String KEY = "spring:session:sessions:*";

    @Test
    void shouldReuseSameSession() {

        EntityExchangeResult<byte[]> first = webTestClient.get()
                .uri(TEST_URL)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .returnResult();

        first.getResponseCookies().forEach((k, v) -> System.out.println(k + " -> " + v));


        String sessionId = first.getResponseCookies()
                .getFirst(SESSION)
                .getValue();

        webTestClient.get()
                .uri(TEST_URL)
                .cookie(SESSION, sessionId)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().doesNotExist(SESSION);
    }

    @Autowired
    ReactiveRedisConnectionFactory connectionFactory;

    @BeforeEach
    void cleanRedis() {
        connectionFactory.getReactiveConnection()
                .serverCommands()
                .flushDb()
                .block();
    }

    @Test
    void shouldCreateOnlyOneSession() {

        webTestClient.get()
                .uri(TEST_URL)
                .exchange()
                .expectStatus().isOk();

        long count = connectionFactory.getReactiveConnection()
                .keyCommands()
                .keys(ByteBuffer.wrap(KEY.getBytes(StandardCharsets.UTF_8)))
                .block()
                .stream()
                .count();

        assertEquals(1, count);
    }

}