package com.example.bankcards.controller;

import com.example.bankcards.dto.AccountDto;
import com.example.bankcards.entity.Account;
import com.example.bankcards.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody Account account) {
        Account createdAccount = accountService.createAccount(account);
        return ResponseEntity.ok(accountService.convertToDto(createdAccount));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable Long id) {
        AccountDto account = accountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountDto> getAccountByNumber(@PathVariable String accountNumber) {
        AccountDto account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountDto>> getUserAccounts(@PathVariable Long userId) {
        List<AccountDto> accounts = accountService.getUserAccounts(userId);
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AccountDto> updateAccountStatus(
            @PathVariable Long id,
            @RequestParam Account.AccountStatus status) {
        Account updatedAccount = accountService.updateAccountStatus(id, status);
        return ResponseEntity.ok(accountService.convertToDto(updatedAccount));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok().build();
    }
}