package com.example.bankcards.service;

import com.example.bankcards.dto.AccountDto;
import com.example.bankcards.entity.Account;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.AccountRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        account = Account.builder()
                .id(1L)
                .accountNumber("12345678901234567890")
                .balance(BigDecimal.valueOf(1000))
                .currency("USD")
                .type(Account.AccountType.CURRENT)
                .status(Account.AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
    }

    @Test
    void createAccount_Success() {
        // Arrange
        Account accountToCreate = Account.builder()
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .type(Account.AccountType.CURRENT)
                .status(Account.AccountStatus.ACTIVE)
                .user(user)
                .build();

        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // Act
        Account result = accountService.createAccount(accountToCreate);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNotNull(result.getAccountNumber());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void getAccountById_Success() {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // Act
        AccountDto result = accountService.getAccountById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("12345678901234567890", result.getAccountNumber());
        assertEquals(BigDecimal.valueOf(1000), result.getBalance());
        assertEquals("USD", result.getCurrency());
    }

    @Test
    void getAccountById_NotFound_ThrowsException() {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.getAccountById(1L);
        });
        assertTrue(exception.getMessage().contains("Account not found"));
    }

    @Test
    void getAccountByNumber_Success() {
        // Arrange
        when(accountRepository.findByAccountNumber("12345678901234567890")).thenReturn(Optional.of(account));

        // Act
        AccountDto result = accountService.getAccountByNumber("12345678901234567890");

        // Assert
        assertNotNull(result);
        assertEquals("12345678901234567890", result.getAccountNumber());
    }

    @Test
    void getUserAccounts_Success() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        when(accountRepository.findByUserId(1L)).thenReturn(List.of(account));

        // Act
        List<AccountDto> result = accountService.getUserAccounts(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getUserAccounts_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.getUserAccounts(1L);
        });
        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void updateAccountStatus_Success() {
        // Arrange
        Account accountToUpdate = Account.builder()
                .id(1L)
                .status(Account.AccountStatus.ACTIVE)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountToUpdate));
        when(accountRepository.save(any(Account.class))).thenReturn(accountToUpdate);

        // Act
        Account result = accountService.updateAccountStatus(1L, Account.AccountStatus.BLOCKED);

        // Assert
        assertNotNull(result);
        assertEquals(Account.AccountStatus.BLOCKED, result.getStatus());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void deleteAccount_Success() {
        // Arrange
        Account accountToDelete = Account.builder()
                .id(1L)
                .balance(BigDecimal.ZERO)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountToDelete));
        doNothing().when(accountRepository).delete(accountToDelete);

        // Act
        accountService.deleteAccount(1L);

        // Assert
        verify(accountRepository, times(1)).delete(accountToDelete);
    }

    @Test
    void deleteAccount_WithPositiveBalance_ThrowsException() {
        // Arrange
        Account accountToDelete = Account.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(100))
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountToDelete));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.deleteAccount(1L);
        });
        assertTrue(exception.getMessage().contains("Cannot delete account with positive balance"));
    }

    @Test
    void convertToDto_Success() {
        // Act
        AccountDto result = accountService.convertToDto(account);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("12345678901234567890", result.getAccountNumber());
        assertEquals(BigDecimal.valueOf(1000), result.getBalance());
        assertEquals("USD", result.getCurrency());
        assertEquals(Account.AccountType.CURRENT, result.getType());
        assertEquals(Account.AccountStatus.ACTIVE, result.getStatus());
        assertEquals(1L, result.getUserId());
        assertEquals("John Doe", result.getUserFullName());
    }
}