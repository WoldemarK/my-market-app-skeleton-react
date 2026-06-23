package ru.yandex.mymarketappskeleton.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.AbstractMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private ReactiveHashOperations<String, Object, Object> hashOperations;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(redisTemplate);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    void plus_shouldIncrementItemCount() {
        when(hashOperations.increment("cart:session1", "1", 1)).thenReturn(Mono.just(2L));

        StepVerifier.create(cartService.plus("session1", 1L)).verifyComplete();

        verify(hashOperations).increment("cart:session1", "1", 1);
    }

    @Test
    void minus_shouldDecreaseCount() {
        when(hashOperations.increment("cart:session1", "1", -1)).thenReturn(Mono.just(2L));

        StepVerifier.create(cartService.minus("session1", 1L)).verifyComplete();

        verify(hashOperations).increment("cart:session1", "1", -1);

        verify(hashOperations, never()).remove(anyString(), anyString());
    }

    @Test
    void minus_shouldRemoveItemWhenCountBecomesZero() {
        when(hashOperations.increment("cart:session1", "1", -1)).thenReturn(Mono.just(0L));

        when(hashOperations.remove("cart:session1", "1")).thenReturn(Mono.just(1L));

        StepVerifier.create(cartService.minus("session1", 1L)).verifyComplete();

        verify(hashOperations).remove("cart:session1", "1");
    }

    @Test
    void delete_shouldRemoveItem() {
        when(hashOperations.remove("cart:session1", "1")).thenReturn(Mono.just(1L));

        StepVerifier.create(cartService.delete("session1", 1L)).verifyComplete();

        verify(hashOperations).remove("cart:session1", "1");
    }

    @Test
    void getRawCart_shouldReturnCartMap() {

        Flux<Map.Entry<Object, Object>> entries = Flux.just(
                new AbstractMap.SimpleEntry<>("1", "2"),
                new AbstractMap.SimpleEntry<>("2", "5")
        );

        when(hashOperations.entries("cart:session1")).thenReturn(entries);

        StepVerifier.create(cartService.getRawCart("session1"))
                .assertNext(cart -> {
                    assert cart.size() == 2;
                    assert cart.get(1L) == 2;
                    assert cart.get(2L) == 5;
                })
                .verifyComplete();
    }

    @Test
    void getCount_shouldReturnValue() {
        when(hashOperations.get("cart:session1", "1")).thenReturn(Mono.just("3"));

        StepVerifier.create(cartService.getCount("session1", 1L))
                .expectNext(3)
                .verifyComplete();
    }

    @Test
    void getCount_shouldReturnZeroWhenItemAbsent() {
        when(hashOperations.get("cart:session1", "1")).thenReturn(Mono.empty());

        StepVerifier.create(cartService.getCount("session1", 1L))
                .expectNext(0)
                .verifyComplete();
    }
}
