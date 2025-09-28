package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Account;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.AccountRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.impl.CardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private CardServiceImpl cardService;

    private User user;
    private Account account;
    private Card card;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        account = Account.builder()
                .id(1L)
                .accountNumber("1234567890")
                .balance(BigDecimal.valueOf(1000))
                .currency("USD")
                .user(user)
                .build();

        card = Card.builder()
                .id(1L)
                .cardHolderName("John Doe")
                .type(Card.CardType.DEBIT)
                .status(Card.CardStatus.ACTIVE)
                .dailyLimit(BigDecimal.valueOf(1000))
                .expiryDate(LocalDate.now().plusYears(3))
                .account(account)
                .build();
    }

    @Test
    void createCard_Success() {
        // Arrange
        Card cardToCreate = Card.builder()
                .cardHolderName("John Doe")
                .type(Card.CardType.DEBIT)
                .account(Account.builder().id(1L).build())
                .build();

        doNothing().when(securityService).checkUserAccess(1L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted-card-number");
        when(cardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        // Act
        Card result = cardService.createCard(cardToCreate);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(cardRepository, times(1)).save(any(Card.class));
        verify(encryptionService, times(1)).encrypt(anyString());
    }

    @Test
    void getCardById_Success() {
        // Arrange
        doNothing().when(securityService).checkCardAccess(1L);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        // Act
        CardDto result = cardService.getCardById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getCardHolderName());
        assertEquals(Card.CardType.DEBIT, result.getType());
    }

    @Test
    void getCardById_NotFound_ThrowsException() {
        // Arrange
        doNothing().when(securityService).checkCardAccess(1L);
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CardNotFoundException.class, () -> {
            cardService.getCardById(1L);
        });
    }

    @Test
    void getAccountCards_Success() {
        // Arrange
        doNothing().when(securityService).checkUserAccess(1L);
        when(cardRepository.findByAccountId(1L)).thenReturn(List.of(card));

        // Act
        List<CardDto> result = cardService.getAccountCards(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getUserCards_Success() {
        // Arrange
        doNothing().when(securityService).checkUserAccess(1L);
        when(cardRepository.findByAccountUserId(1L)).thenReturn(List.of(card));

        // Act
        List<CardDto> result = cardService.getUserCards(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getUserCardsPaginated_Success() {
        // Arrange
        Page<Card> cardPage = new PageImpl<>(List.of(card));

        doNothing().when(securityService).checkUserAccess(1L);
        when(cardRepository.findByAccountUserId(anyLong(), any(Pageable.class))).thenReturn(cardPage);

        // Act
        Page<CardDto> result = cardService.getUserCardsPaginated(1L, null, Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getId());

        verify(cardRepository, times(1)).findByAccountUserId(anyLong(), any(Pageable.class));
    }

    @Test
    void updateCardStatus_Success() {
        // Arrange
        doNothing().when(securityService).checkCardAccess(1L);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        // Act
        Card result = cardService.updateCardStatus(1L, Card.CardStatus.BLOCKED);

        // Assert
        assertNotNull(result);
        assertEquals(Card.CardStatus.BLOCKED, result.getStatus());
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void deleteCard_Success() {
        // Arrange
        doNothing().when(securityService).checkCardAccess(1L);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        doNothing().when(cardRepository).delete(card);

        // Act
        cardService.deleteCard(1L);

        // Assert
        verify(cardRepository, times(1)).delete(card);
    }

    @Test
    void convertToDto_Success() {
        // Act
        CardDto result = cardService.convertToDto(card);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getCardHolderName());
        assertEquals(Card.CardType.DEBIT, result.getType());
        assertEquals(Card.CardStatus.ACTIVE, result.getStatus());
        assertEquals(BigDecimal.valueOf(1000), result.getDailyLimit());
        assertEquals(1L, result.getAccountId());
        assertEquals("1234567890", result.getAccountNumber());
        assertEquals(BigDecimal.valueOf(1000), result.getBalance());
    }
}