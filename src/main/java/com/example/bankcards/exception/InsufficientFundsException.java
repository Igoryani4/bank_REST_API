package com.example.bankcards.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(BigDecimal currentBalance, BigDecimal requiredAmount) {
        super(String.format("Insufficient funds. Current balance: %.2f, Required: %.2f",
                currentBalance, requiredAmount));
    }
}