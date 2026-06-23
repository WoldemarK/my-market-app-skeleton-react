package ru.yandex.mymarketappskeleton.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.session.WebSessionManager;

@Component
@RequiredArgsConstructor
public class SessionDebug {

    private final WebSessionManager webSessionManager;

    @PostConstruct
    public void test() {
        System.out.println("WEB SESSION MANAGER = " + webSessionManager);
    }
}
