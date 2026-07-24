package ru.yandex.shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.shop.dto.ItemDto;
import ru.yandex.shop.dto.Paging;
import ru.yandex.shop.enums.ActionType;
import ru.yandex.shop.enums.SortType;
import ru.yandex.shop.service.CartIdService;
import ru.yandex.shop.service.CartService;
import ru.yandex.shop.service.ItemService;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class ItemsController {

    private final ItemService itemService;
    private final CartService cartService;
    private final CartIdService cartIdService;

    @GetMapping({"/", "/items"})
    public Mono<String> getItems(@RequestParam(required = false) String search,
                                 @RequestParam(defaultValue = "NO") SortType sort,
                                 @RequestParam(defaultValue = "1") int pageNumber,
                                 @RequestParam(defaultValue = "5") int pageSize,
                                 Authentication authentication,
                                 WebSession session,
                                 Model model) {

        return Mono.defer(() -> {

            String cartId = cartIdService.getCartId(authentication, session);

            return itemService.findItems(search, sort, pageNumber, pageSize)
                    .flatMap(page ->
                            Flux.fromIterable(page.content())
                                    .flatMap(item ->
                                            cartService.getCount(cartId, item.id())
                                                    .map(count ->
                                                            ItemDto.builder()
                                                                    .id(item.id())
                                                                    .title(item.title())
                                                                    .description(item.description())
                                                                    .imgPath(item.imgPath())
                                                                    .price(item.price())
                                                                    .count(count)
                                                                    .build()
                                                    )
                                    )
                                    .collectList()
                                    .flatMap(items ->
                                            itemService.groupItems(Flux.fromIterable(items))
                                                    .map(grouped -> {

                                                        model.addAttribute("items", grouped);
                                                        model.addAttribute("search", search);
                                                        model.addAttribute("sort", sort);
                                                        model.addAttribute(
                                                                "paging",
                                                                new Paging(
                                                                        pageNumber,
                                                                        pageSize,
                                                                        page.hasNext(),
                                                                        page.hasPrevious()
                                                                )
                                                        );

                                                        return "items";
                                                    })
                                    )
                    );
        });
    }

    @PostMapping("/items")
    public Mono<String> updateFromItems(ServerWebExchange exchange, Authentication authentication) {
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

                                String search = form.getFirst("search");
                                String sort = form.getFirst("sort");
                                String pageNumber = form.getFirst("pageNumber");
                                String pageSize = form.getFirst("pageSize");

                                return operation.thenReturn(
                                        "redirect:/items?search=%s&sort=%s&pageNumber=%s&pageSize=%s"
                                                .formatted(
                                                        search,
                                                        sort,
                                                        pageNumber,
                                                        pageSize
                                                )
                                );
                            });
                });
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable Long id,
                                Authentication authentication,
                                WebSession session,
                                Model model) {

        return Mono.defer(() -> {

            String cartId = cartIdService.getCartId(authentication, session);

            return itemService.findById(id)
                    .zipWith(cartService.getCount(cartId, id))
                    .map(tuple -> {

                        ItemDto item = tuple.getT1();

                        ItemDto itemWithCount = ItemDto.builder()
                                .id(item.id())
                                .title(item.title())
                                .description(item.description())
                                .imgPath(item.imgPath())
                                .price(item.price())
                                .count(tuple.getT2())
                                .build();

                        model.addAttribute("item", itemWithCount);

                        return "item";
                    });
        });
    }
}
