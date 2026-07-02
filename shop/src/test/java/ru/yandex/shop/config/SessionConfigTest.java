package ru.yandex.shop.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

import static org.junit.jupiter.api.Assertions.*;

class SessionConfigTest {

    private final SessionConfig sessionConfig = new SessionConfig();

    @Test
    void shouldCreateWebSessionIdResolver() {

        WebSessionIdResolver resolver = sessionConfig.webSessionIdResolver();

        assertNotNull(resolver);
        assertInstanceOf(CookieWebSessionIdResolver.class, resolver);
    }
}