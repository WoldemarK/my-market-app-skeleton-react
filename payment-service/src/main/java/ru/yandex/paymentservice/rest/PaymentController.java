package ru.yandex.paymentservice.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

import ru.shop.payment.dto.BalanceResponse;
import ru.shop.payment.dto.PaymentRequest;
import ru.shop.payment.dto.PaymentResponse;
import ru.yandex.paymentservice.service.PaymentService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentController  {

    private final PaymentService paymentService;

    @GetMapping("/balance")
    public Mono<ResponseEntity<BalanceResponse>> balance(@AuthenticationPrincipal Jwt jwt) {
        String clientId = jwt.getClaimAsString("azp");
        return Mono.just(ResponseEntity.ok(paymentService.getBalanceResponse(clientId)));
    }

    @PostMapping("/payment")
    public Mono<ResponseEntity<PaymentResponse>> payment(@Valid @RequestBody Mono<PaymentRequest> request,
                                                         @AuthenticationPrincipal Jwt jwt) {
        String clientId = jwt.getClaimAsString("azp");

        return request.map(paymentRequest ->
                ResponseEntity.ok(paymentService.makePayment(clientId, paymentRequest))
        );
    }
}
