package ru.yandex.mymarketappskeleton.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.mymarketappskeleton.dto.ItemDto;
import ru.yandex.mymarketappskeleton.dto.Paging;
import ru.yandex.mymarketappskeleton.enums.ActionType;
import ru.yandex.mymarketappskeleton.enums.SortType;
import ru.yandex.mymarketappskeleton.service.CartService;
import ru.yandex.mymarketappskeleton.service.ItemService;


@Controller
@RequiredArgsConstructor
public class ItemsController {

    private final ItemService itemService;
    private final CartService cartService;


    @GetMapping({"/", "/items"})
    public Mono<String> getItems(@RequestParam(required = false) String search,
                                 @RequestParam(defaultValue = "NO") SortType sort,
                                 @RequestParam(defaultValue = "1") int pageNumber,
                                 @RequestParam(defaultValue = "5") int pageSize,
                                 WebSession session,
                                 Model model) {

        return Mono.defer(() -> {

            String sessionId = session.getId();

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
    public Mono<String> updateFromItems(ServerWebExchange exchange,
                                        WebSession session) {

        return Mono.defer(() ->
                exchange.getFormData()
                        .flatMap(form -> {

                            Long id = Long.valueOf(form.getFirst("id"));

                            ActionType action = ActionType.valueOf(form.getFirst("action"));

                            Mono<Void> operation = switch (action) {
                                case PLUS -> cartService.plus(session.getId(), id);
                                case MINUS -> cartService.minus(session.getId(), id);
                                case DELETE -> cartService.delete(session.getId(), id);
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
                        })
        );
    }


    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable Long id, WebSession session, Model model) {

        return Mono.defer(() -> {

            String sessionId = session.getId();

            return itemService.findById(id)
                    .zipWith(cartService.getCount(sessionId, id))
                    .map(tuple -> {

                        ItemDto item = tuple.getT1();
                        item.setCount(tuple.getT2());

                        model.addAttribute("item", item);

                        return "item";
                    });
        });
    }


    @PostMapping("/items/{id}")
    public Mono<String> updateItem(@PathVariable Long id, @RequestParam ActionType action, WebSession session) {

        return Mono.defer(() -> {

            Mono<Void> operation = switch (action) {
                case PLUS -> cartService.plus(session.getId(), id);
                case MINUS -> cartService.minus(session.getId(), id);
                case DELETE -> cartService.delete(session.getId(), id);
            };

            return operation.thenReturn("redirect:/items/" + id);
        });
    }
}
