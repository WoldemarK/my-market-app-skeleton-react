package ru.yandex.paymentservice.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import ru.shop.payment.dto.BalanceResponse;
import ru.yandex.paymentservice.rest.PaymentController;
import ru.yandex.paymentservice.service.PaymentService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        BalanceResponse response = new BalanceResponse();
        response.setBalance(BigDecimal.valueOf(1000));

        when(paymentService.getBalance()).thenReturn(response);

        StepVerifier.create(controller.getBalance(null))
                .assertNext(res -> {
                    assertEquals(BigDecimal.valueOf(1000),
                            res.getBody().getBalance());
                })
                .verifyComplete();
    }
}
