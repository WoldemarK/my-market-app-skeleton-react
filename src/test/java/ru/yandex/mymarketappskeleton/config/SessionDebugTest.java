package ru.yandex.mymarketappskeleton.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.session.WebSessionManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SessionDebugTest {
    @Test
    void testMethodShouldNotThrowException() {

        WebSessionManager manager = mock(WebSessionManager.class);
        SessionDebug sessionDebug = new SessionDebug(manager);
        assertDoesNotThrow(sessionDebug::test);
    }
}