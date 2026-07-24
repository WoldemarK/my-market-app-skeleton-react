package ru.yandex.paymentservice.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.paymentservice.exception.InsufficientBalanceException;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class Account{

    private String ownerId;
    private BigDecimal balance;


    public void withdraw(BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Not enough balance");
        }

        balance = balance.subtract(amount);
    }


}