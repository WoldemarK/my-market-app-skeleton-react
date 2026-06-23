package ru.yandex.mymarketappskeleton.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final ReactiveStringRedisTemplate redisTemplate;

    private String key(String sessionId) {
        return "cart:" + sessionId;
    }

    public Mono<Void> plus(String sessionId, Long itemId) {
        return redisTemplate.opsForHash()
                .increment(key(sessionId), itemId.toString(), 1)
                .doOnNext(value -> log.info("PLUS itemId={}, sessionId={}, value={}",
                                itemId,
                                sessionId,
                                value
                        )
                ).then();

    }

    public Mono<Void> minus(String sessionId, Long itemId) {

        String key = key(sessionId);
        String field = itemId.toString();

        return redisTemplate.opsForHash()
                .increment(key, field, -1)
                .flatMap(value -> {

                    log.info("MINUS itemId={}, sessionId={}, value={}",
                            itemId,
                            sessionId,
                            value);

                    if (value <= 0) {
                        return redisTemplate.opsForHash()
                                .remove(key, field)
                                .then();
                    }

                    return Mono.empty();
                })
                .then();
    }


    public Mono<Void> delete(String sessionId, Long itemId) {
        return redisTemplate.opsForHash()
                .remove(key(sessionId),
                        itemId.toString()
                )
                .doOnSuccess(v -> log.info("DELETE itemId={}, sessionId={}",
                                itemId,
                                sessionId
                        )
                ).then();
    }

    public Mono<Void> clear(String sessionId) {
        return redisTemplate.opsForHash()
                .delete(key(sessionId))
                .doOnSuccess(v -> log.info("CLEAR cart sessionId={}", sessionId))
                .then();
    }

    public Mono<Map<Long, Integer>> getRawCart(String sessionId) {
        return redisTemplate.opsForHash()
                .entries(key(sessionId))
                .collectMap(
                        entry -> Long.parseLong(entry.getKey().toString()),
                        entry -> Integer.parseInt(entry.getValue().toString())
                ).doOnNext(cart -> log.debug("GET CART sessionId={}, items={}", sessionId, cart));
    }

    public Mono<Integer> getCount(String sessionId, Long itemId) {
        return redisTemplate.opsForHash()
                .get(key(sessionId), itemId.toString())
                .map(value -> Integer.parseInt(value.toString()))
                .defaultIfEmpty(0);

    }
}
