package ru.yandex.mymarketappskeleton.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.mymarketappskeleton.service.OrderService;

@Controller
@RequiredArgsConstructor
public class BuyController {

    private final OrderService orderService;

    @PostMapping("/buy")
    public Mono<String> buy(WebSession session) {
        return orderService.createOrder(session.getId())
                .map(orderId -> "redirect:/orders/" + orderId + "?newOrder=true");
    }
}
