package ru.yandex.paymentservice.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.shop.payment.dto.BalanceResponse;
import ru.shop.payment.dto.PaymentRequest;
import ru.shop.payment.dto.PaymentResponse;
import ru.yandex.paymentservice.exception.InsufficientBalanceException;
import ru.yandex.paymentservice.rest.PaymentController;
import ru.yandex.paymentservice.service.PaymentService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerUnitTest {

    @Mock
    private PaymentService paymentService;

    private PaymentController controller;

    @BeforeEach
    void setUp() {
        controller = new PaymentController(paymentService);
    }

    @Test
    void shouldReturnBalance() {
        BalanceResponse expectedResponse = new BalanceResponse();
        expectedResponse.setBalance(BigDecimal.valueOf(1000));

        when(paymentService.getBalanceResponse())
                .thenReturn(expectedResponse);


        StepVerifier.create(controller.getBalance(null))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(200, response.getStatusCode().value());
                    assertNotNull(response.getBody());
                    assertEquals(BigDecimal.valueOf(1000), response.getBody().getBalance());
                })
                .verifyComplete();
    }

    @Test
    void shouldMakePaymentSuccessfully() {

        PaymentRequest request = new PaymentRequest();
        request.setAmount(BigDecimal.valueOf(1000));

        PaymentResponse expectedResponse = new PaymentResponse();
        expectedResponse.setSuccess(true);
        expectedResponse.setMessage("Payment request succeeded");

        when(paymentService.makePayment(request))
                .thenReturn(expectedResponse);

        StepVerifier.create(controller.makePayment(Mono.just(request), null))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(200, response.getStatusCode().value());
                    assertNotNull(response.getBody());
                    assertEquals(true, response.getBody().getSuccess());
                    assertEquals("Payment request succeeded", response.getBody().getMessage());

                }).verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenInsufficientBalance() {

        PaymentRequest request = new PaymentRequest();
        request.setAmount(BigDecimal.valueOf(1500));

        when(paymentService.makePayment(request))
                .thenThrow(new InsufficientBalanceException("Not enough balance. Current: 1000, Required: 1500"));


        StepVerifier.create(controller.makePayment(Mono.just(request), null))
                .expectError(InsufficientBalanceException.class)
                .verify();
    }

    @Test
    void shouldReturnBalanceWithDifferentValues() {

        BalanceResponse expectedResponse = new BalanceResponse();
        expectedResponse.setBalance(BigDecimal.valueOf(500.50));

        when(paymentService.getBalanceResponse()).thenReturn(expectedResponse);

        StepVerifier.create(controller.getBalance(null))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(200, response.getStatusCode().value());
                    assertNotNull(response.getBody());
                    assertEquals(BigDecimal.valueOf(500.50), response.getBody().getBalance());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnZeroBalance() {

        BalanceResponse expectedResponse = new BalanceResponse();
        expectedResponse.setBalance(BigDecimal.ZERO);

        when(paymentService.getBalanceResponse()).thenReturn(expectedResponse);

        StepVerifier.create(controller.getBalance(null))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(200, response.getStatusCode().value());
                    assertNotNull(response.getBody());
                    assertEquals(BigDecimal.ZERO, response.getBody().getBalance());
                })
                .verifyComplete();
    }
}
