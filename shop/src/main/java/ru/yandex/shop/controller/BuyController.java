package ru.yandex.shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.shop.service.CartIdService;
import ru.yandex.shop.service.OrderService;

@Controller
@RequiredArgsConstructor
public class BuyController {

    private final OrderService orderService;
    private final CartIdService cartIdService;

    @PostMapping("/buy")
    public Mono<String> buy(WebSession session, Authentication authentication){
        return Mono.defer(() -> {
            String cartId = cartIdService.getCartId(authentication, session);
            return orderService.createOrder(cartId)
                    .map("redirect:/orders/%d?newOrder=true"::formatted);

        });
    }
}
