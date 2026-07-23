package ru.yandex.paymentservice.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import ru.shop.payment.dto.BalanceResponse;
import ru.shop.payment.dto.PaymentRequest;
import ru.shop.payment.dto.PaymentResponse;
import ru.yandex.paymentservice.exception.AccountNotFoundException;
import ru.yandex.paymentservice.exception.InsufficientBalanceException;
import ru.yandex.paymentservice.model.Account;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@Getter
@Setter
@ConfigurationProperties(prefix = "payment")
public class PaymentService {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @PostConstruct
    void init(){
        accounts.put(
                "shop-app",
                new Account(
                        "shop-app",
                        new BigDecimal("10000")
                )
        );
    }



    public BalanceResponse getBalanceResponse(String clientId) {
        Account account = getAccount(clientId);
        BalanceResponse response = new BalanceResponse();
        response.setBalance(account.getBalance());
        return response;
    }



    public PaymentResponse makePayment(String ownerId, PaymentRequest request) {
        Account account = getAccount(ownerId);
        synchronized (account) {
            account.withdraw(request.getAmount());

            log.info("Payment completed client={} amount={}", ownerId, request.getAmount());
        }
        PaymentResponse response = new PaymentResponse();
        response.setSuccess(true);
        response.setMessage("Payment completed");

        return response;
    }



    private Account getAccount(String ownerId) {
        Account account = accounts.get(ownerId);
        if(account == null) {
            throw new AccountNotFoundException("Account not found: " + ownerId);
        }
        return account;
    }
}
