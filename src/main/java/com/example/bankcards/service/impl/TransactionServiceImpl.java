package com.example.bankcards.service.impl;

import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Account;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.repository.AccountRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public TransactionDto transfer(TransferRequest transferRequest) {
        try {
            // Находим счета
            Account fromAccount = accountRepository.findByAccountNumber(transferRequest.getFromAccountNumber())
                    .orElseThrow(() -> new RuntimeException("From account not found: " + transferRequest.getFromAccountNumber()));

            Account toAccount = accountRepository.findByAccountNumber(transferRequest.getToAccountNumber())
                    .orElseThrow(() -> new RuntimeException("To account not found: " + transferRequest.getToAccountNumber()));

            // Проверяем возможность перевода
            validateTransfer(fromAccount, toAccount, transferRequest.getAmount());

            // Выполняем перевод
            fromAccount.setBalance(fromAccount.getBalance().subtract(transferRequest.getAmount()));
            toAccount.setBalance(toAccount.getBalance().add(transferRequest.getAmount()));

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            // Создаем транзакцию
            Transaction transaction = Transaction.builder()
                    .amount(transferRequest.getAmount())
                    .currency(fromAccount.getCurrency())
                    .type(Transaction.TransactionType.TRANSFER)
                    .status(Transaction.TransactionStatus.COMPLETED)
                    .description(transferRequest.getDescription() != null ?
                            transferRequest.getDescription() : "Transfer to " + toAccount.getAccountNumber())
                    .fromAccount(fromAccount)
                    .toAccount(toAccount)
                    .build();

            Transaction savedTransaction = transactionRepository.save(transaction);

            log.info("Transfer completed: {} from {} to {}",
                    transferRequest.getAmount(),
                    transferRequest.getFromAccountNumber(),
                    transferRequest.getToAccountNumber());

            return convertToDto(savedTransaction);

        } catch (Exception e) {
            log.error("Transfer failed: {}", e.getMessage());
            throw new RuntimeException("Transfer failed: " + e.getMessage(), e);
        }
    }

    @Override
    public TransactionDto getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        return convertToDto(transaction);
    }

    @Override
    public List<TransactionDto> getUserTransactions(Long userId) {
        return transactionRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDto> getAccountTransactions(String accountNumber) {
        return transactionRepository.findByFromAccountAccountNumberOrToAccountAccountNumber(accountNumber, accountNumber).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDto> getTransactionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionDto convertToDto(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setTransactionId(transaction.getTransactionId());
        dto.setAmount(transaction.getAmount());
        dto.setCurrency(transaction.getCurrency());
        dto.setType(transaction.getType());
        dto.setStatus(transaction.getStatus());
        dto.setDescription(transaction.getDescription());
        dto.setCreatedAt(transaction.getCreatedAt());

        if (transaction.getFromAccount() != null) {
            dto.setFromAccountId(transaction.getFromAccount().getId());
            dto.setFromAccountNumber(transaction.getFromAccount().getAccountNumber());
        }

        if (transaction.getToAccount() != null) {
            dto.setToAccountId(transaction.getToAccount().getId());
            dto.setToAccountNumber(transaction.getToAccount().getAccountNumber());
        }

        return dto;
    }

    private void validateTransfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        // Проверка статуса счетов
        if (fromAccount.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new RuntimeException("From account is not active");
        }

        if (toAccount.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new RuntimeException("To account is not active");
        }

        // Проверка валюты
        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            throw new RuntimeException("Currency mismatch between accounts");
        }

        // Проверка достаточности средств
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        // Проверка положительной суммы
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be positive");
        }

        // Проверка перевода на тот же счет
        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new RuntimeException("Cannot transfer to the same account");
        }
    }
}
