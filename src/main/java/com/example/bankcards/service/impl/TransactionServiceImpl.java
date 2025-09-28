package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardToAccountTransferRequest;
import com.example.bankcards.dto.CardToCardTransferRequest;
import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Account;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.exception.AccountNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.TransferValidationException;
import com.example.bankcards.repository.AccountRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.service.SecurityService;
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
    private final SecurityService securityService;
    private final CardRepository cardRepository;

    @Override
    @Transactional
    public TransactionDto transfer(TransferRequest transferRequest) {
        Long currentUserId = null;

        try {
            currentUserId = securityService.getCurrentUserId();

            Account fromAccount = accountRepository.findByAccountNumber(transferRequest.getFromAccountNumber())
                    .orElseThrow(() -> new AccountNotFoundException(transferRequest.getFromAccountNumber()));

            Account toAccount = accountRepository.findByAccountNumber(transferRequest.getToAccountNumber())
                    .orElseThrow(() -> new AccountNotFoundException(transferRequest.getToAccountNumber()));

            validateCardOwnership(fromAccount, toAccount, currentUserId);
            validateTransfer(fromAccount, toAccount, transferRequest.getAmount());

            fromAccount.setBalance(fromAccount.getBalance().subtract(transferRequest.getAmount()));
            toAccount.setBalance(toAccount.getBalance().add(transferRequest.getAmount()));

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

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

            log.info("Transfer completed: {} {} from {} to {} for user {}",
                    transferRequest.getAmount(),
                    fromAccount.getCurrency(),
                    transferRequest.getFromAccountNumber(),
                    transferRequest.getToAccountNumber(),
                    currentUserId);

            return convertToDto(savedTransaction);

        } catch (AccountNotFoundException | InsufficientFundsException | TransferValidationException e) {
            log.error("Transfer failed for user {}: {}", currentUserId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Transfer failed for user {}: {}", currentUserId, e.getMessage());
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
        securityService.checkUserAccess(userId);
        return transactionRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDto> getAccountTransactions(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
        securityService.checkUserAccess(account.getUser().getId());

        return transactionRepository.findByFromAccountAccountNumberOrToAccountAccountNumber(accountNumber, accountNumber).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDto> getTransactionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        securityService.checkUserAccess(userId);
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

    @Override
    @Transactional
    public TransactionDto cardToCardTransfer(CardToCardTransferRequest request) {
        Long currentUserId = securityService.getCurrentUserId();

        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + request.getFromCardId()));

        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + request.getToCardId()));

        securityService.checkCardAccess(request.getFromCardId());

        if (!fromCard.getAccount().getUser().getId().equals(currentUserId) ||
                !toCard.getAccount().getUser().getId().equals(currentUserId)){
            throw new RuntimeException("Cannot transfer between cards of different users");
        }

        Account fromAccount = fromCard.getAccount();
        Account toAccount = toCard.getAccount();

        return performTransfer(fromAccount, toAccount, request.getAmount(),
                request.getDescription() != null ? request.getDescription() :
                        "Card-to-card transfer to " + toCard.getMaskedCardNumber());
    }

    private TransactionDto performTransfer(Account fromAccount, Account toAccount,
                                           BigDecimal amount, String description) {
        validateTransfer(fromAccount, toAccount, amount);

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction = Transaction.builder()
                .amount(amount)
                .currency(fromAccount.getCurrency())
                .type(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.COMPLETED)
                .description(description)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Card transfer completed: {} {} from account {} to account {}",
                amount, fromAccount.getCurrency(), fromAccount.getId(), toAccount.getId());

        return convertToDto(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionDto cardToAccountTransfer(CardToAccountTransferRequest request) {
        Long currentUserId = securityService.getCurrentUserId();

        // Получаем карту отправителя
        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + request.getFromCardId()));

        // Проверяем доступ к карте
        securityService.checkCardAccess(request.getFromCardId());

        // Получаем счет получателя по номеру
        Account toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(request.getToAccountNumber()));

        Account fromAccount = fromCard.getAccount();

        return performTransfer(fromAccount, toAccount, request.getAmount(),
                request.getDescription() != null ? request.getDescription() :
                        "Transfer to account " + request.getToAccountNumber());
    }

    private void validateCardOwnership(Account fromAccount, Account toAccount, Long currentUserId) {
        if (!fromAccount.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("From account does not belong to current user");
        }

        if (!toAccount.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("To account does not belong to current user");
        }

        log.debug("Card ownership validation passed for user: {}", currentUserId);
    }

    private void validateTransfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        if (fromAccount.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new RuntimeException("From account is not active");
        }

        if (toAccount.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new RuntimeException("To account is not active");
        }

        validateCardStatus(fromAccount);

        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            throw new TransferValidationException("Currency mismatch between accounts");
        }

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(fromAccount.getBalance(), amount);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be positive");
        }

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new RuntimeException("Cannot transfer to the same account");
        }
    }

    private void validateCardStatus(Account fromAccount) {
        if (fromAccount.getCards() != null && !fromAccount.getCards().isEmpty()) {
            boolean hasActiveCard = fromAccount.getCards().stream()
                    .anyMatch(card -> card.getStatus() == Card.CardStatus.ACTIVE);
            if (!hasActiveCard) {
                throw new RuntimeException("No active cards for from account");
            }
        } else {
            throw new RuntimeException("From account has no cards");
        }

    }
}