package com.example.bankcards.service;

import com.example.bankcards.dto.CardToAccountTransferRequest;
import com.example.bankcards.dto.CardToCardTransferRequest;
import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
    TransactionDto transfer(TransferRequest transferRequest);
    TransactionDto getTransactionById(Long id);
    List<TransactionDto> getUserTransactions(Long userId);
    List<TransactionDto> getAccountTransactions(String accountNumber);
    List<TransactionDto> getTransactionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    TransactionDto convertToDto(Transaction transaction);
    TransactionDto cardToCardTransfer(CardToCardTransferRequest request);
    TransactionDto cardToAccountTransfer(CardToAccountTransferRequest request);
}
