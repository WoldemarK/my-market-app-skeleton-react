package ru.yandex.paymentservice.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.shop.payment.api.DefaultApi;
import ru.shop.payment.dto.BalanceResponse;
import ru.shop.payment.dto.PaymentRequest;
import ru.shop.payment.dto.PaymentResponse;
import ru.yandex.paymentservice.service.PaymentService;

@RestController
@RequiredArgsConstructor
public class PaymentController implements DefaultApi {

    private final PaymentService paymentService;

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(ServerWebExchange exchange) {
        return Mono.just
                (
                        ResponseEntity.ok(paymentService.getBalance()
                        )
                );
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> makePayment(Mono<PaymentRequest> paymentRequest,
                                                             ServerWebExchange exchange
    ) {
        return paymentRequest
                .map(paymentService::makePayment)
                .map(ResponseEntity::ok);
    }
}
