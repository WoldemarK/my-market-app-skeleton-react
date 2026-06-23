package ru.yandex.mymarketappskeleton.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.mymarketappskeleton.service.OrderService;


@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Mono<String> getOrders(Model model) {
        return orderService.findAll()
                .collectList()
                .doOnNext(orders -> model.addAttribute("orders", orders))
                .thenReturn("orders");
    }
    @GetMapping("/{id}")
    public Mono<String> getOrder(@PathVariable Long id,
                                 @RequestParam(defaultValue = "false") boolean newOrder,
                                 Model model) {
        return orderService.findById(id)
                .doOnNext(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("newOrder", newOrder);
                })
                .thenReturn("order");
    }
}
