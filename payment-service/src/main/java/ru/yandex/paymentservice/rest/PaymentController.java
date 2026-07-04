package ru.yandex.paymentservice.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.shop.payment.api.DefaultApi;
import ru.shop.payment.dto.BalanceResponse;
import ru.shop.payment.dto.PaymentRequest;
import ru.shop.payment.dto.PaymentResponse;
import ru.yandex.paymentservice.service.PaymentService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentController implements DefaultApi {

    private final PaymentService paymentService;

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(ServerWebExchange exchange) {
        log.info("GET /api/payment/balance");

        return Mono.just(ResponseEntity.ok(paymentService.getBalanceResponse()));
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> makePayment(Mono<PaymentRequest> paymentRequest,
                                                             ServerWebExchange exchange) {
        return paymentRequest
                .doOnNext(request ->
                        log.info("POST /api/payment/pay, amount={}",
                                request.getAmount())
                )
                .map(paymentService::makePayment)
                .map(ResponseEntity::ok)
                .doOnSuccess(response ->
                        log.info("Payment processed successfully")
                )
                .doOnError(error -> log.error("Payment processing failed: {}",
                        error.getMessage()));
    }
}
