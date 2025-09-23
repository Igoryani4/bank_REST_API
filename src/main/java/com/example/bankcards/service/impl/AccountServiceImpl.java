package com.example.bankcards.service.impl;

import com.example.bankcards.dto.AccountDto;
import com.example.bankcards.entity.Account;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.AccountRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Account createAccount(Account account) {
        try {
            // Генерация номера счета
            String accountNumber = generateAccountNumber();
            account.setAccountNumber(accountNumber);

            Account savedAccount = accountRepository.save(account);
            log.info("Created account: {} for user: {}", accountNumber, account.getUser().getId());
            return savedAccount;
        } catch (Exception e) {
            log.error("Error creating account for user: {}", account.getUser().getId(), e);
            throw new RuntimeException("Failed to create account", e);
        }
    }

    @Override
    public AccountDto getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
        return convertToDto(account);
    }

    @Override
    public AccountDto getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found with number: " + accountNumber));
        return convertToDto(account);
    }

    @Override
    public List<AccountDto> getUserAccounts(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }

        return accountRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Account updateAccountStatus(Long accountId, Account.AccountStatus status) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

        account.setStatus(status);
        Account updatedAccount = accountRepository.save(account);
        log.info("Updated account status: {} to {}", accountId, status);
        return updatedAccount;
    }

    @Override
    @Transactional
    public void deleteAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

        if (account.getBalance().compareTo(java.math.BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Cannot delete account with positive balance");
        }

        accountRepository.delete(account);
        log.info("Deleted account: {}", accountId);
    }

    @Override
    public AccountDto convertToDto(Account account) {
        AccountDto dto = new AccountDto();
        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBalance(account.getBalance());
        dto.setCurrency(account.getCurrency());
        dto.setType(account.getType());
        dto.setStatus(account.getStatus());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUserId(account.getUser().getId());
        dto.setUserFullName(account.getUser().getFirstName() + " " + account.getUser().getLastName());
        return dto;
    }

    private String generateAccountNumber() {
        Random random = new Random();
        String number;
        do {
            number = "40817" + String.format("%013d", random.nextLong(1000000000000L));
        } while (accountRepository.existsByAccountNumber(number));

        return number;
    }
}