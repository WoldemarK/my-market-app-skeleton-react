package ru.yandex.paymentservice.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/balance")
public class BalanceController {

    @GetMapping("/{id}")
    public Mono<String> getBalance(@PathVariable String id) {
        return Mono.just("100");
    }
}
