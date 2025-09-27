package com.example.bankcards.controller;

import com.example.bankcards.dto.AccountDto;
import com.example.bankcards.entity.Account;
import com.example.bankcards.service.AccountService;
import com.example.bankcards.service.SecurityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;
    private final SecurityService securityService;

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody Account account) {
        securityService.checkAdminAccess();
        Account createdAccount = accountService.createAccount(account);
        return ResponseEntity.ok(accountService.convertToDto(createdAccount));
    }

    @GetMapping("/my-accounts")
    public ResponseEntity<List<AccountDto>> getMyAccounts() {
        Long userId = securityService.getCurrentUserId();
        log.info("getMyAccounts: userId = {}", userId);
        List<AccountDto> accounts = accountService.getUserAccounts(userId);
        log.info("getMyAccounts: accounts = {}", accounts.size(), userId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable Long id) {
        securityService.checkAdminAccess();
        AccountDto account = accountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountDto> getAccountByNumber(@PathVariable String accountNumber) {
        securityService.checkAdminAccess();
        AccountDto account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountDto>> getUserAccounts(@PathVariable Long userId) {
        securityService.checkAdminAccess();
        List<AccountDto> accounts = accountService.getUserAccounts(userId);
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AccountDto> updateAccountStatus(
            @PathVariable Long id,
            @RequestParam Account.AccountStatus status) {
        securityService.checkAdminAccess();
        Account updatedAccount = accountService.updateAccountStatus(id, status);
        return ResponseEntity.ok(accountService.convertToDto(updatedAccount));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        securityService.checkAdminAccess();
        accountService.deleteAccount(id);
        return ResponseEntity.ok().build();
    }
}