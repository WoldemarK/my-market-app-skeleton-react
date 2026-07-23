package ru.yandex.shop.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.shop.dto.ItemDto;
import ru.yandex.shop.enums.ActionType;
import ru.yandex.shop.repository.ItemRepository;
import ru.yandex.shop.service.CartIdService;
import ru.yandex.shop.service.CartService;

import java.math.BigDecimal;
import java.util.Objects;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final ItemRepository itemRepository;
    private final CartIdService cartIdService;

    @GetMapping("/items")
    public Mono<String> getCart(Model model, WebSession session, Authentication authentication) {

        return Mono.defer(() -> {

            String cartId = cartIdService.getCartId(authentication, session);

            return cartService.getRawCart(cartId)

                    .flatMap(cart ->
                            itemRepository.findAllById(cart.keySet())
                                    .map(item ->
                                            ItemDto.builder()
                                                    .id(item.getId())
                                                    .title(item.getTitle())
                                                    .description(item.getDescription())
                                                    .imgPath(item.getImgPath())
                                                    .price(item.getPrice())
                                                    .count(cart.get(item.getId()))
                                                    .build()
                                    )
                                    .collectList()

                                    .map(items -> {

                                        BigDecimal total = items.stream()
                                                .map(i ->
                                                        i.price().multiply(BigDecimal.valueOf(i.count()))
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
    public Mono<String> updateCart(ServerWebExchange exchange, Authentication authentication) {

        return exchange.getFormData()
                .flatMap(form -> {

                    Long id = Long.valueOf(Objects.requireNonNull(form.getFirst("id")));

                    ActionType action = ActionType.valueOf(form.getFirst("action"));

                    return exchange.getSession()
                            .flatMap(session -> {

                                String cartId = cartIdService.getCartId(authentication, session);

                                Mono<Void> operation = switch (action) {
                                    case PLUS -> cartService.plus(cartId, id);
                                    case MINUS -> cartService.minus(cartId, id);
                                    case DELETE -> cartService.delete(cartId, id);
                                };
                                return operation.thenReturn("redirect:/cart/items");

                            });
                });
    }
}
