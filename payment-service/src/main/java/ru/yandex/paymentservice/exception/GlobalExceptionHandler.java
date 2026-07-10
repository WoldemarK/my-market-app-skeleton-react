package ru.yandex.paymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, String>>  handleInsufficientBalance(InsufficientBalanceException ex) {

        Map<String, String> body = new HashMap<>();
        body.put("code", "INSUFFICIENT_BALANCE");
        body.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(body);
    }

}
