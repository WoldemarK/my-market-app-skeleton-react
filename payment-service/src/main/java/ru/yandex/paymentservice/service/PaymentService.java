package ru.yandex.paymentservice.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import ru.shop.payment.dto.BalanceResponse;
import ru.shop.payment.dto.PaymentRequest;
import ru.shop.payment.dto.PaymentResponse;
import ru.yandex.paymentservice.exception.InsufficientBalanceException;


import java.math.BigDecimal;

@Slf4j
@Service
@Getter
@Setter
@ConfigurationProperties(prefix = "payment")
public class PaymentService {

    private BigDecimal balance;

    public BalanceResponse getBalance() {

        log.info("Balance requested: {}", balance);

        var result = new BalanceResponse();
        result.setBalance(balance);

        return result;
    }

    public PaymentResponse makePayment(PaymentRequest request) {

        log.info("Payment request: {}", request);

            if (balance.compareTo(request.getAmount()) < 0) {
                throw new InsufficientBalanceException("Not enough balance");
            }

        balance = balance.subtract(request.getAmount());

        PaymentResponse response = new PaymentResponse();
        response.setSuccess(true);
        response.setMessage("Payment successful");

        return response;
    }
}
