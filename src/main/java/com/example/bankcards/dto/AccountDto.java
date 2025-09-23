package com.example.bankcards.dto;

import com.example.bankcards.entity.Account;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class AccountDto {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private String currency;
    private Account.AccountType type;
    private Account.AccountStatus status;
    private LocalDateTime createdAt;
    private Long userId;
    private String userFullName;

}
