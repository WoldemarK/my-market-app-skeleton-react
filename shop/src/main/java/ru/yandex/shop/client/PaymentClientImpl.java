package ru.yandex.shop.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.shop.payment.dto.BalanceResponse;
import ru.shop.payment.dto.PaymentRequest;
import ru.shop.payment.dto.PaymentResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentClientImpl implements PaymentClient {

    private final WebClient paymentWebClient;

    @Override
    public Mono<BalanceResponse> getBalance() {

        log.info("Getting balance from payment service");

        return paymentWebClient.get()
                .uri("/api/payment/balance")
                .retrieve()
                .bodyToMono(BalanceResponse.class)
                .doOnSuccess(response ->
                        log.info("Balance received: {}", response.getBalance())
                )
                .doOnError(error -> log.error("Error getting balance: {}", error.getMessage()));
    }

    @Override
    public Mono<PaymentResponse> pay(PaymentRequest request) {

        log.info("Sending payment request: amount={}", request.getAmount());

        return paymentWebClient.post()
                .uri("/api/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .doOnSuccess(response ->
                        log.info("Payment response: success={}", response.getSuccess()))
                .doOnError(error ->
                        log.error("Payment error: {}", error.getMessage()));
    }
}
