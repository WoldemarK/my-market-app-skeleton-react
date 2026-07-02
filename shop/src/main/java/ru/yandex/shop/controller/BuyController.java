package ru.yandex.shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.shop.service.OrderService;

@Controller
@RequiredArgsConstructor
public class BuyController {

    private final OrderService orderService;

    @PostMapping("/buy")
    public Mono<String> buy(WebSession session) {
        return Mono.defer(() ->
                orderService.createOrder(session.getId())
                        .map("redirect:/orders/%d?newOrder=true"::formatted)
        );
    }
}
