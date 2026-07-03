package ru.yandex.shop.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@RestController
public class TestController {

    @GetMapping("/test")
    public Mono<String> test(WebSession session) {
        session.getAttributes().put("test", "value");
        return Mono.just(session.getId());
    }
}
