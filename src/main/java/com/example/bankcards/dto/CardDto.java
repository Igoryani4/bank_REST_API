package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CardDto {
    private Long id;
    private String maskedCardNumber;
    private LocalDate expiryDate;
    private String cardHolderName;
    private Card.CardType type;
    private Card.CardStatus status;
    private BigDecimal dailyLimit;
    private LocalDateTime createdAt;
    private Long accountId;
    private String accountNumber;
    private BigDecimal balance;
}
