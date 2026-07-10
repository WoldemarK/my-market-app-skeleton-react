package ru.yandex.shop.config;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class SessionInitializer implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        return exchange.getSession()
                .doOnNext(session -> {
                    session.start();
                    session.getAttributes()
                            .putIfAbsent("created", true);
                })
                .then(chain.filter(exchange));
    }
}
