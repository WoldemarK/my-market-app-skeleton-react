package ru.yandex.shop.config.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.shop.payment.dto.BalanceResponse;
import ru.shop.payment.dto.PaymentRequest;
import ru.shop.payment.dto.PaymentResponse;
import ru.yandex.shop.client.PaymentClient;

@Service
@RequiredArgsConstructor
public class PaymentClientImpl implements PaymentClient {
    private final WebClient paymentWebClient;

    @Override
    public Mono<PaymentResponse> pay(PaymentRequest request) {
        return paymentWebClient.post()
                .uri("/api/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class);
    }

    @Override
    public Mono<BalanceResponse> getBalance() {
        return null;
    }
}
