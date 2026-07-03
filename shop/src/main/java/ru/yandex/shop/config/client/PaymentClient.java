package ru.yandex.shop.config.client;

import reactor.core.publisher.Mono;
import ru.shop.payment.dto.PaymentRequest;
import ru.shop.payment.dto.PaymentResponse;

public interface PaymentClient {
    Mono<PaymentResponse> pay(PaymentRequest request);
}
