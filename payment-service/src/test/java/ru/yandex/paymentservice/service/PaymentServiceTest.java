package ru.yandex.paymentservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.shop.payment.dto.BalanceResponse;
import ru.shop.payment.dto.PaymentRequest;
import ru.shop.payment.dto.PaymentResponse;
import ru.yandex.paymentservice.exception.InsufficientBalanceException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
        paymentService.setBalance(BigDecimal.valueOf(1000));
    }
    @Test
    void shouldReturnCurrentBalance(){
        BalanceResponse balanceResponse = paymentService.getBalance();
        assertNotNull(balanceResponse);
        assertEquals(BigDecimal.valueOf(1000), balanceResponse.getBalance());
    }

    @Test
    void shouldMakePayment() {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(BigDecimal.valueOf(300));

        PaymentResponse response = paymentService.makePayment(request);

        assertTrue(response.getSuccess());
        assertEquals("Payment successful", response.getMessage());
        assertEquals(BigDecimal.valueOf(700), paymentService.getBalance().getBalance());
    }

    @Test
    void shouldMakePaymentEqualToBalance() {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(BigDecimal.valueOf(1000));

        PaymentResponse response = paymentService.makePayment(request);

        assertTrue(response.getSuccess());
        assertEquals(BigDecimal.ZERO, paymentService.getBalance().getBalance());
    }

    @Test
    void shouldThrowExceptionWhenBalanceIsInsufficient() {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(BigDecimal.valueOf(1500));

        InsufficientBalanceException exception = assertThrows(
                InsufficientBalanceException.class,
                () -> paymentService.makePayment(request)
        );

        assertEquals("Not enough balance", exception.getMessage());
        assertEquals(BigDecimal.valueOf(1000), paymentService.getBalance().getBalance());
    }
}