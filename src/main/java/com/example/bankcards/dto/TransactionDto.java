package com.example.bankcards.dto;

import com.example.bankcards.entity.Transaction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDto {
    private Long id;
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private Transaction.TransactionType type;
    private Transaction.TransactionStatus status;
    private String description;
    private LocalDateTime createdAt;
    private Long fromAccountId;
    private String fromAccountNumber;
    private Long toAccountId;
    private String toAccountNumber;
}