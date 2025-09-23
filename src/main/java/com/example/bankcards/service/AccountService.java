package com.example.bankcards.service;

import com.example.bankcards.dto.AccountDto;
import com.example.bankcards.entity.Account;
import com.example.bankcards.entity.User;

import java.util.List;

public interface AccountService {
    Account createAccount(Account account);
    AccountDto getAccountById(Long id);
    AccountDto getAccountByNumber(String accountNumber);
    List<AccountDto> getUserAccounts(Long userId);
    Account updateAccountStatus(Long accountId, Account.AccountStatus status);
    void deleteAccount(Long accountId);
    AccountDto convertToDto(Account account);
}