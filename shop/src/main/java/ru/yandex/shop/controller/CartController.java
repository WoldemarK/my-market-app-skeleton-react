package ru.yandex.shop.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.shop.dto.ItemDto;
import ru.yandex.shop.enums.ActionType;
import ru.yandex.shop.repository.ItemRepository;
import ru.yandex.shop.service.CartService;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final ItemRepository itemRepository;

    @GetMapping("/items")
    public Mono<String> getCart(Model model, WebSession session) {

        return Mono.defer(() -> {

            String sessionId = session.getId();

            return cartService.getRawCart(sessionId)
                    .flatMap(cart ->
                            itemRepository.findAllById(cart.keySet())
                                    .map(item -> ItemDto.builder()
                                            .id(item.getId())
                                            .title(item.getTitle())
                                            .description(item.getDescription())
                                            .imgPath(item.getImgPath())
                                            .price(item.getPrice())
                                            .count(cart.get(item.getId()))
                                            .build())
                                    .collectList()
                                    .map(items -> {

                                        BigDecimal total = items.stream()
                                                .map(i ->
                                                        BigDecimal.valueOf(i.price().doubleValue())
                                                                .multiply(BigDecimal.valueOf(i.count()))
                                                )
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        model.addAttribute("items", items);
                                        model.addAttribute("total", total);

                                        return "cart";
                                    })
                    );
        });
    }

    @PostMapping("/items")
    public Mono<String> updateCart(@RequestParam Long id,
                                   @RequestParam ActionType action,
                                   WebSession session) {

        return Mono.defer(() -> {

            String sessionId = session.getId();

            Mono<Void> operation =
                    switch (action) {
                        case PLUS -> cartService.plus(sessionId, id);
                        case MINUS -> cartService.minus(sessionId, id);
                        case DELETE -> cartService.delete(sessionId, id);
                    };

            return operation.thenReturn("redirect:/cart/items");
        });
    }
}
