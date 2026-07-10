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
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@Getter
@Setter
@ConfigurationProperties(prefix = "payment")
public class PaymentService {

    private final AtomicReference<BigDecimal> balance = new AtomicReference<>();

    public void setBalance(BigDecimal balance) {
        this.balance.set(balance);
    }

    public BigDecimal getBalance() {
        log.info("Balance requested: {}", balance.get());
        return balance.get();
    }

    public BalanceResponse getBalanceResponse() {
        BalanceResponse response = new BalanceResponse();
        response.setBalance(getBalance());
        return response;
    }

    public synchronized PaymentResponse makePayment(PaymentRequest request) {
        log.info("Payment request: {}", request);

        BigDecimal amount = request.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        BigDecimal currentBalance = balance.get();

        if (currentBalance.compareTo(amount) < 0) {
            String message = String.format("Not enough balance. Current: %s, Required: %s",
                    currentBalance, amount);
            log.warn(message);

            throw new InsufficientBalanceException(message);
        }

        BigDecimal newBalance = currentBalance.subtract(amount);
        balance.set(newBalance);

        PaymentResponse response = new PaymentResponse();
        response.setSuccess(true);
        response.setMessage("Payment request succeeded");

        log.info("Payment completed. New balance: {}", newBalance);

        return response;
    }

    public synchronized void addBalance(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        BigDecimal newBalance = balance.get().add(amount);
        balance.set(newBalance);
        log.info("Balance added: {}. New balance: {}", amount, newBalance);
    }
}
