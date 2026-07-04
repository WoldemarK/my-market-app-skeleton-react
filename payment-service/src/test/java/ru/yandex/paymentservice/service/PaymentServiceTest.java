package ru.yandex.paymentservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.shop.payment.dto.PaymentRequest;
import ru.shop.payment.dto.PaymentResponse;
import ru.yandex.paymentservice.exception.InsufficientBalanceException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
        paymentService.setBalance(BigDecimal.valueOf(1000));
    }

    @Test
    void shouldReturnCurrentBalance() {

        BigDecimal balance = paymentService.getBalance();

        assertThat(balance).isEqualTo(BigDecimal.valueOf(1000));
    }

    @Test
    void shouldMakePaymentSuccessfullyWhenBalanceIsSufficient() {

        PaymentRequest request = new PaymentRequest();
        request.setAmount(BigDecimal.valueOf(1000));

        PaymentResponse response = paymentService.makePayment(request);

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Payment request succeeded");
        assertThat(paymentService.getBalance()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void shouldThrowExceptionWhenBalanceIsInsufficient() {

        PaymentRequest request = new PaymentRequest();
        request.setAmount(BigDecimal.valueOf(1500));

        assertThatThrownBy(() -> paymentService.makePayment(request))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("Not enough balance. Current: 1000, Required: 1500");

        assertThat(paymentService.getBalance()).isEqualTo(BigDecimal.valueOf(1000));
    }

    @Test
    void shouldAddBalanceCorrectly() {

        paymentService.addBalance(BigDecimal.valueOf(500));

        assertThat(paymentService.getBalance()).isEqualTo(BigDecimal.valueOf(1500));
    }

    @Test
    void shouldHandleMultiplePaymentsCorrectly() {

        PaymentRequest request1 = new PaymentRequest();
        request1.setAmount(BigDecimal.valueOf(300));

        PaymentRequest request2 = new PaymentRequest();
        request2.setAmount(BigDecimal.valueOf(200));

        PaymentResponse response1 = paymentService.makePayment(request1);
        PaymentResponse response2 = paymentService.makePayment(request2);

        assertThat(response1.getSuccess()).isTrue();
        assertThat(response2.getSuccess()).isTrue();
        assertThat(paymentService.getBalance()).isEqualTo(BigDecimal.valueOf(500));
    }

    @Test

    void shouldThrowExceptionWithCorrectMessage() {

        PaymentRequest request = new PaymentRequest();
        request.setAmount(BigDecimal.valueOf(2000));

        InsufficientBalanceException exception = assertThrows(
                InsufficientBalanceException.class,
                () -> paymentService.makePayment(request)
        );

        assertEquals("Not enough balance. Current: 1000, Required: 2000", exception.getMessage());
    }

}