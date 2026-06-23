package ru.yandex.mymarketappskeleton.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.mymarketappskeleton.dto.ItemDto;
import ru.yandex.mymarketappskeleton.enums.ActionType;
import ru.yandex.mymarketappskeleton.repository.ItemRepository;
import ru.yandex.mymarketappskeleton.service.CartService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final ItemRepository itemRepository;

    @GetMapping("/items")
    public Mono<String> getCart(Model model, WebSession session) {

        String sessionId = session.getId();

        return cartService.getRawCart(sessionId)
                .flatMap(cart ->
                        itemRepository.findAllById(cart.keySet())
                                .map(item -> ItemDto.builder()
                                        .id(item.getId())
                                        .title(item.getTitle())
                                        .description(item.getDescription())
                                        .imgPath(item.getImgPath())
                                        .price(item.getPrice().doubleValue())
                                        .count(cart.get(item.getId()))
                                        .build())
                                .collectList()
                                .map(items -> {

                                    BigDecimal total = items.stream()
                                            .map(i -> BigDecimal.valueOf(i.getPrice())
                                                    .multiply(BigDecimal.valueOf(i.getCount())))
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                                    model.addAttribute("items", items);
                                    model.addAttribute("total", total);

                                    return "cart";
                                }));
    }

    @PostMapping("/items")
    public Mono<String> updateCart(@RequestParam Long id, @RequestParam ActionType action, WebSession session) {
        String sessionId = session.getId();
        Mono<Void> result = switch (action) {
            case PLUS -> cartService.plus(sessionId, id);
            case MINUS -> cartService.minus(sessionId, id);
            case DELETE -> cartService.delete(sessionId, id);
        };

        return result.thenReturn("redirect:/cart/items");
    }
}
