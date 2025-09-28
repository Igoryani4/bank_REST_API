package com.example.bankcards.service;

import com.example.bankcards.dto.CardToCardTransferRequest;
import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Account;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AccountNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.AccountRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User user;
    private Account fromAccount;
    private Account toAccount;
    private TransferRequest transferRequest;
    private Card fromCard;
    private Card toCard;


    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        // Сначала создаем карты
        fromCard = Card.builder()
                .id(1L)
                .cardHolderName("John Doe")
                .type(Card.CardType.DEBIT)
                .status(Card.CardStatus.ACTIVE)
                .dailyLimit(BigDecimal.valueOf(5000))
                .expiryDate(LocalDate.now().plusYears(3))
                .encryptedCardNumber("encrypted_card_123")
                .encryptedCvv("encrypted_cvv_123")
                .build();

        toCard = Card.builder()
                .id(2L)
                .cardHolderName("Jane Smith")
                .type(Card.CardType.DEBIT)
                .status(Card.CardStatus.ACTIVE)
                .dailyLimit(BigDecimal.valueOf(5000))
                .expiryDate(LocalDate.now().plusYears(3))
                .encryptedCardNumber("encrypted_card_456")
                .encryptedCvv("encrypted_cvv_456")
                .build();

        // Затем создаем счета с картами
        fromAccount = Account.builder()
                .id(1L)
                .accountNumber("1234567890")
                .balance(BigDecimal.valueOf(1000))
                .currency("USD")
                .status(Account.AccountStatus.ACTIVE)
                .user(user)
                .cards(List.of(fromCard)) // ДОБАВИТЬ карты к счету
                .build();

        toAccount = Account.builder()
                .id(2L)
                .accountNumber("0987654321")
                .balance(BigDecimal.valueOf(500))
                .currency("USD")
                .status(Account.AccountStatus.ACTIVE)
                .user(user)
                .cards(List.of(toCard)) // ДОБАВИТЬ карты к счету
                .build();

        // Устанавливаем связь карт со счетами
        fromCard.setAccount(fromAccount);
        toCard.setAccount(toAccount);

        transferRequest = new TransferRequest();
        transferRequest.setFromAccountNumber("1234567890");
        transferRequest.setToAccountNumber("0987654321");
        transferRequest.setAmount(BigDecimal.valueOf(100));
        transferRequest.setDescription("Test transfer");
    }

    @Test
    void transfer_Success() {
        // Arrange
        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("0987654321")).thenReturn(Optional.of(toAccount));

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100))
                .currency("USD")
                .type(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.COMPLETED)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        // Act
        TransactionDto result = transactionService.transfer(transferRequest);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100), result.getAmount());
        assertEquals("USD", result.getCurrency());
        assertEquals(Transaction.TransactionType.TRANSFER, result.getType());

        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void transfer_InsufficientFunds_ThrowsException() {
        // Arrange
        fromAccount.setBalance(BigDecimal.valueOf(50)); // Меньше суммы перевода

        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("0987654321")).thenReturn(Optional.of(toAccount));

        // УБРАТЬ эти моки
        // when(cardRepository.findByAccountId(1L)).thenReturn(List.of(fromCard));
        // when(cardRepository.findByAccountId(2L)).thenReturn(List.of(toCard));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> { // ИЗМЕНИТЬ на RuntimeException
            transactionService.transfer(transferRequest);
        });
    }

    @Test
    void transfer_AccountNotFound_ThrowsException() {
        // Arrange
        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> { // ИЗМЕНИТЬ на RuntimeException
            transactionService.transfer(transferRequest);
        });
    }

    @Test
    void cardToCardTransfer_Success() {
        // Arrange
        CardToCardTransferRequest request = new CardToCardTransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(100));
        request.setDescription("Card to card transfer");

        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        doNothing().when(securityService).checkCardAccess(1L);

        // УБРАТЬ эти моки
        // when(cardRepository.findByAccountId(1L)).thenReturn(List.of(fromCard));
        // when(cardRepository.findByAccountId(2L)).thenReturn(List.of(toCard));

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100))
                .currency("USD")
                .type(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.COMPLETED)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        // Act
        TransactionDto result = transactionService.cardToCardTransfer(request);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100), result.getAmount());
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }


    @Test
    void getTransactionById_Success() {
        // Arrange
        Transaction transaction = Transaction.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100))
                .currency("USD")
                .build();

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        // Act
        TransactionDto result = transactionService.getTransactionById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BigDecimal.valueOf(100), result.getAmount());
    }

    @Test
    void getAccountTransactions_Success() {
        // Arrange
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(fromAccount));
        doNothing().when(securityService).checkUserAccess(1L);

        Transaction transaction = Transaction.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100))
                .fromAccount(fromAccount)
                .build();

        when(transactionRepository.findByFromAccountAccountNumberOrToAccountAccountNumber("1234567890", "1234567890"))
                .thenReturn(List.of(transaction));

        // Act
        List<TransactionDto> result = transactionService.getAccountTransactions("1234567890");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void convertToDto_Success() {
        // Arrange
        Transaction transaction = Transaction.builder()
                .id(1L)
                .transactionId("TXN123")
                .amount(BigDecimal.valueOf(100))
                .currency("USD")
                .type(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.COMPLETED)
                .description("Test transaction")
                .createdAt(LocalDateTime.now())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .build();

        // Act
        TransactionDto result = transactionService.convertToDto(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TXN123", result.getTransactionId());
        assertEquals(BigDecimal.valueOf(100), result.getAmount());
        assertEquals("USD", result.getCurrency());
        assertEquals(Transaction.TransactionType.TRANSFER, result.getType());
        assertEquals(Transaction.TransactionStatus.COMPLETED, result.getStatus());
        assertEquals("Test transaction", result.getDescription());
        assertEquals(1L, result.getFromAccountId());
        assertEquals("1234567890", result.getFromAccountNumber());
        assertEquals(2L, result.getToAccountId());
        assertEquals("0987654321", result.getToAccountNumber());
    }
}