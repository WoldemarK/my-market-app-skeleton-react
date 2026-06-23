package ru.yandex.mymarketappskeleton.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.mymarketappskeleton.dto.CartUpdateRequest;
import ru.yandex.mymarketappskeleton.dto.ItemDto;
import ru.yandex.mymarketappskeleton.enums.ActionTypes;
import ru.yandex.mymarketappskeleton.enums.SortType;
import ru.yandex.mymarketappskeleton.service.CartService;
import ru.yandex.mymarketappskeleton.service.ItemService;


@Controller
@RequiredArgsConstructor
public class ItemsController {

    private final ItemService itemService;
    private final CartService cartService;


    @GetMapping({"/", "/items"})
    public Mono<String> getItems(@RequestParam(name = "search", required = false) String search,
                                 @RequestParam(name = "sort", defaultValue = "NO") SortType sort,
                                 @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
                                 @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
                                 WebSession session,
                                 Model model) {

        String sessionId = session.getId();
        System.out.println("GET SESSION = " + session.getId());
        return itemService.findItems(search, sort, pageNumber, pageSize)
                .flatMap(page ->
                        Flux.fromIterable(page.content())
                                .flatMap(item ->
                                        cartService.getCount(sessionId, item.getId())
                                                .map(count -> {
                                                    item.setCount(count);
                                                    return item;
                                                })
                                )
                                .collectList()
                                .flatMap(items ->
                                        itemService.groupItems(Flux.fromIterable(items))
                                                .map(grouped -> {
                                                    model.addAttribute("items", grouped);
                                                    model.addAttribute("search", search);
                                                    model.addAttribute("sort", sort);
                                                    model.addAttribute("paging", page);
                                                    return "items";
                                                })
                                )
                );
    }

    @PostMapping("/items")
    public Mono<String> updateFromItems(
            ServerWebExchange exchange,
            WebSession session) {

        return exchange.getFormData()
                .flatMap(form -> {

                    Long id = Long.valueOf(form.getFirst("id"));
                    ActionTypes action =
                            ActionTypes.valueOf(form.getFirst("action"));

                    Mono<Void> operation = switch (action) {
                        case PLUS -> cartService.plus(session.getId(), id);
                        case MINUS -> cartService.minus(session.getId(), id);
                    };

                    return operation.thenReturn("redirect:/items");
                });
    }


    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable("id") Long id, WebSession session, Model model) {

        return itemService.findById(id)
                .zipWith(cartService.getCount(session.getId(), id))
                .map(tuple -> {

                    ItemDto item = tuple.getT1();
                    item.setCount(tuple.getT2());

                    return item;
                })
                .map(item -> {
                    model.addAttribute("item", item);
                    return "item";
                });
    }


    @PostMapping("/items/{id}")
    public Mono<String> updateItem(@PathVariable("id") Long id,
                                   @RequestParam("action") ActionTypes action,
                                   WebSession session) {
        Mono<Void> operation = switch (action) {
            case PLUS -> cartService.plus(session.getId(), id);
            case MINUS -> cartService.minus(session.getId(), id);

        };

        return operation.thenReturn("redirect:/items/" + id);
    }
}
