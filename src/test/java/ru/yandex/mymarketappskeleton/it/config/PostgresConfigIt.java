package ru.yandex.mymarketappskeleton.it.config;

import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureWebTestClient
public class PostgresConfigIt {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("market-app")
            .withUsername("user")
            .withPassword("123");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://" + postgres.getHost() + ":" +
                        postgres.getFirstMappedPort() + "/" + postgres.getDatabaseName());

        r.add("spring.r2dbc.username", postgres::getUsername);
        r.add("spring.r2dbc.password", postgres::getPassword);
    }
}
