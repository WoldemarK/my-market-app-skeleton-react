package ru.yandex.shop.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;
import ru.yandex.paymentservice.exception.InsufficientBalanceException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientBalanceException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleInsufficientBalance(InsufficientBalanceException ex) {
        log.warn("Insufficient balance: {}", ex.getMessage());

        Map<String, String> body = new HashMap<>();
        body.put("code", "INSUFFICIENT_BALANCE");
        body.put("message", ex.getMessage());

        return Mono.just(ResponseEntity
                .status(HttpStatus.PAYMENT_REQUIRED)
                .body(body));
    }

    @ExceptionHandler(EmptyCartException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleEmptyCart(EmptyCartException ex) {
        log.warn("Empty cart: {}", ex.getMessage());

        Map<String, String> body = new HashMap<>();
        body.put("code", "EMPTY_CART");
        body.put("message", ex.getMessage());

        return Mono.just(ResponseEntity
                .badRequest()
                .body(body));
    }

    @ExceptionHandler(PaymentFailedException.class)
    public Mono<ResponseEntity<Map<String, String>>> handlePaymentFailed(PaymentFailedException ex) {
        log.error("Payment failed: {}", ex.getMessage());

        Map<String, String> body = new HashMap<>();
        body.put("code", "PAYMENT_FAILED");
        body.put("message", ex.getMessage());

        return Mono.just(ResponseEntity
                .status(HttpStatus.PAYMENT_REQUIRED)
                .body(body));
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleItemNotFound(ItemNotFoundException ex) {
        log.warn("Item not found: {}", ex.getMessage());

        Map<String, String> body = new HashMap<>();
        body.put("code", "ITEM_NOT_FOUND");
        body.put("message", ex.getMessage());

        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleOrderNotFound(OrderNotFoundException ex) {
        log.warn("Order not found: {}", ex.getMessage());

        Map<String, String> body = new HashMap<>();
        body.put("code", "ORDER_NOT_FOUND");
        body.put("message", ex.getMessage());

        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body));
    }

//    @ExceptionHandler(Exception.class)
//    public Mono<ResponseEntity<Map<String, String>>> handleGenericException(Exception ex) {
//        log.error("Unexpected error: {}", ex.getMessage(), ex);
//
//        Map<String, String> body = new HashMap<>();
//        body.put("code", "INTERNAL_ERROR");
//        body.put("message", "An unexpected error occurred");
//
//        return Mono.just(ResponseEntity
//                .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(body));
//    }
}
