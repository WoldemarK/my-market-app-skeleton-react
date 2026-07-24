package ru.yandex.paymentservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.shop.payment.dto.BalanceResponse;
import ru.shop.payment.dto.PaymentRequest;
import ru.shop.payment.dto.PaymentResponse;
import ru.yandex.paymentservice.exception.AccountNotFoundException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
        paymentService.init();
    }

    @Test
    void shouldReturnBalance() {

        BalanceResponse response = paymentService.getBalanceResponse("shop-app");

        assertNotNull(response);
        assertEquals(new BigDecimal("10000"), response.getBalance());
    }

    @Test
    void shouldMakePayment() {

        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("100"));

        PaymentResponse response = paymentService.makePayment("shop-app", request);

        assertEquals("Payment completed", response.getMessage());

        BalanceResponse balance = paymentService.getBalanceResponse("shop-app");

        assertEquals(new BigDecimal("9900"), balance.getBalance());
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFoundForBalance() {

        assertThrows(
                AccountNotFoundException.class,
                () -> paymentService.getBalanceResponse("unknown")
        );
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFoundForPayment() {

        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("100"));

        assertThrows(
                AccountNotFoundException.class,
                () -> paymentService.makePayment("unknown", request)
        );
    }

    @Test
    void shouldUpdateBalanceAfterSeveralPayments() {

        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("500"));

        paymentService.makePayment("shop-app", request);
        paymentService.makePayment("shop-app", request);

        BalanceResponse balance =
                paymentService.getBalanceResponse("shop-app");

        assertEquals(new BigDecimal("9000"), balance.getBalance());
    }

}