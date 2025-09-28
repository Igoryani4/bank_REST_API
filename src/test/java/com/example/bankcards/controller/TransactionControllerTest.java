package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Account;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.SecurityService;
import com.example.bankcards.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private SecurityService securityService;

    @MockBean
    private CardRepository cardRepository;

    private TransactionDto transactionDto;
    private TransferRequest transferRequest;
    private CardToCardTransferRequest cardToCardRequest;
    private CardToAccountTransferRequest cardToAccountRequest;

    @BeforeEach
    void setUp() {
        transactionDto = new TransactionDto();
        transactionDto.setId(1L);
        transactionDto.setTransactionId("TXN123456");
        transactionDto.setAmount(BigDecimal.valueOf(100.0));
        transactionDto.setCurrency("USD");
        transactionDto.setType(Transaction.TransactionType.TRANSFER);
        transactionDto.setStatus(Transaction.TransactionStatus.COMPLETED);
        transactionDto.setDescription("Test transfer");
        transactionDto.setCreatedAt(LocalDateTime.now());
        transactionDto.setFromAccountId(1L);
        transactionDto.setFromAccountNumber("1234567890");
        transactionDto.setToAccountId(2L);
        transactionDto.setToAccountNumber("0987654321");

        transferRequest = new TransferRequest();
        transferRequest.setFromAccountNumber("1234567890");
        transferRequest.setToAccountNumber("0987654321");
        transferRequest.setAmount(BigDecimal.valueOf(100.0));
        transferRequest.setDescription("Test transfer");

        cardToCardRequest = new CardToCardTransferRequest();
        cardToCardRequest.setFromCardId(1L);
        cardToCardRequest.setToCardId(2L);
        cardToCardRequest.setAmount(BigDecimal.valueOf(50.0));
        cardToCardRequest.setDescription("Card to card transfer");

        cardToAccountRequest = new CardToAccountTransferRequest();
        cardToAccountRequest.setFromCardId(1L);
        cardToAccountRequest.setToAccountNumber("1234567890");
        cardToAccountRequest.setAmount(BigDecimal.valueOf(75.0));
        cardToAccountRequest.setDescription("Card to account transfer");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void transfer_Success() throws Exception {
        when(transactionService.transfer(any(TransferRequest.class))).thenReturn(transactionDto);

        mockMvc.perform(post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.transactionId").value("TXN123456"))
                .andExpect(jsonPath("$.type").value("TRANSFER"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void cardToCardTransfer_Success() throws Exception {
        when(transactionService.cardToCardTransfer(any(CardToCardTransferRequest.class))).thenReturn(transactionDto);

        mockMvc.perform(post("/transactions/card-to-card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardToCardRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(100.0));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void cardToAccountTransfer_Success() throws Exception {
        when(transactionService.cardToAccountTransfer(any(CardToAccountTransferRequest.class))).thenReturn(transactionDto);

        mockMvc.perform(post("/transactions/card-to-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardToAccountRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCardTransactions_Success() throws Exception {
        // Создаем мок карты и аккаунта
        Card card = mock(Card.class);
        Account account = mock(Account.class);

        when(card.getId()).thenReturn(1L);
        when(card.getAccount()).thenReturn(account);
        when(account.getAccountNumber()).thenReturn("1234567890");

        when(cardRepository.findById(anyLong())).thenReturn(Optional.of(card));
        doNothing().when(securityService).checkCardAccess(anyLong());

        List<TransactionDto> transactions = Arrays.asList(transactionDto);

        // Используем существующий метод getAccountTransactions
        when(transactionService.getAccountTransactions(anyString())).thenReturn(transactions);

        mockMvc.perform(get("/transactions/admin/card/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].transactionId").value("TXN123456"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTransactionById_Success() throws Exception {
        when(transactionService.getTransactionById(anyLong())).thenReturn(transactionDto);

        mockMvc.perform(get("/transactions/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTransactionsByDateRange_Success() throws Exception {
        List<TransactionDto> transactions = Arrays.asList(transactionDto);
        when(transactionService.getTransactionsByDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(transactions);

        mockMvc.perform(get("/transactions/admin/user/1/date-range")
                        .param("startDate", "2024-01-01T00:00:00")
                        .param("endDate", "2024-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].amount").value(100.0));
    }
}