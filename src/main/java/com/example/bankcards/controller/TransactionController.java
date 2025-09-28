package com.example.bankcards.controller;

import com.example.bankcards.dto.CardToAccountTransferRequest;
import com.example.bankcards.dto.CardToCardTransferRequest;
import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.SecurityService;
import com.example.bankcards.service.TransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;
    private final SecurityService securityService;
    private final CardRepository cardRepository;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionDto> transfer(@Valid @RequestBody TransferRequest transferRequest) {
        TransactionDto transaction = transactionService.transfer(transferRequest);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/card-to-card")
    public ResponseEntity<TransactionDto> cardToCardTransfer(
            @Valid @RequestBody CardToCardTransferRequest request) {
        TransactionDto transaction = transactionService.cardToCardTransfer(request);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/card-to-account")
    public ResponseEntity<TransactionDto> cardToAccountTransfer(
            @Valid @RequestBody CardToAccountTransferRequest request) {
        TransactionDto transaction = transactionService.cardToAccountTransfer(request);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/admin/card/{cardId}")
    public ResponseEntity<List<TransactionDto>> getCardTransactions(@PathVariable Long cardId) {
        securityService.checkCardAccess(cardId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        List<TransactionDto> transactions = transactionService.getAccountTransactions(card.getAccount().getAccountNumber());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(@PathVariable Long id) {
        TransactionDto transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/admin/user/{userId}")
    public ResponseEntity<List<TransactionDto>> getUserTransactions(@PathVariable Long userId) {
        List<TransactionDto> transactions = transactionService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("admin/account/{accountNumber}")
    public ResponseEntity<List<TransactionDto>> getAccountTransactions(@PathVariable String accountNumber) {
        List<TransactionDto> transactions = transactionService.getAccountTransactions(accountNumber);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("admin/user/{userId}/date-range")
    public ResponseEntity<List<TransactionDto>> getTransactionsByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<TransactionDto> transactions = transactionService.getTransactionsByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }
}