package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.SecurityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    @MockBean
    private SecurityService securityService;

    private Card card;
    private CardDto cardDto;
    private Card blockedCard;
    private CardDto blockedCardDto;

    @BeforeEach
    void setUp() {
        card = new Card();
        card.setId(1L);
        card.setCardNumber("4111111111111111");
        card.setMaskedCardNumber("411111******1111");
        card.setExpiryDate(LocalDate.now().plusYears(3));
        card.setCardHolderName("John Doe");
        card.setType(Card.CardType.DEBIT);
        card.setStatus(Card.CardStatus.ACTIVE);
        card.setDailyLimit(BigDecimal.valueOf(1000.0));
        card.setCreatedAt(LocalDateTime.now());

        cardDto = new CardDto();
        cardDto.setId(1L);
        cardDto.setCardNumber("4111111111111111");
        cardDto.setMaskedCardNumber("411111******1111");
        cardDto.setExpiryDate(LocalDate.now().plusYears(3));
        cardDto.setCardHolderName("John Doe");
        cardDto.setType(Card.CardType.DEBIT);
        cardDto.setStatus(Card.CardStatus.ACTIVE);
        cardDto.setDailyLimit(BigDecimal.valueOf(1000.0));
        cardDto.setCreatedAt(LocalDateTime.now());
        cardDto.setAccountId(1L);
        cardDto.setAccountNumber("1234567890");
        cardDto.setBalance(BigDecimal.valueOf(500.0));

        blockedCard = new Card();
        blockedCard.setId(1L);
        blockedCard.setCardNumber("4111111111111111");
        blockedCard.setMaskedCardNumber("411111******1111");
        blockedCard.setExpiryDate(LocalDate.now().plusYears(3));
        blockedCard.setCardHolderName("John Doe");
        blockedCard.setType(Card.CardType.DEBIT);
        blockedCard.setStatus(Card.CardStatus.BLOCKED);
        blockedCard.setDailyLimit(BigDecimal.valueOf(1000.0));
        blockedCard.setCreatedAt(LocalDateTime.now());

        blockedCardDto = new CardDto();
        blockedCardDto.setId(1L);
        blockedCardDto.setCardNumber("4111111111111111");
        blockedCardDto.setMaskedCardNumber("411111******1111");
        blockedCardDto.setExpiryDate(LocalDate.now().plusYears(3));
        blockedCardDto.setCardHolderName("John Doe");
        blockedCardDto.setType(Card.CardType.DEBIT);
        blockedCardDto.setStatus(Card.CardStatus.BLOCKED);
        blockedCardDto.setDailyLimit(BigDecimal.valueOf(1000.0));
        blockedCardDto.setCreatedAt(LocalDateTime.now());
        blockedCardDto.setAccountId(1L);
        blockedCardDto.setAccountNumber("1234567890");
        blockedCardDto.setBalance(BigDecimal.valueOf(500.0));
    }

//    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_AdminAccess_Success() throws Exception {
        doNothing().when(securityService).checkAdminAccess();
        when(cardService.createCard(any(Card.class))).thenReturn(card);
        when(cardService.convertToDto(any(Card.class))).thenReturn(cardDto);

        String cardJson = """
        {
            "cardNumber": "4111111111111111",
            "expiryDate": "2028-09-28",
            "cardHolderName": "John Doe",
            "type": "DEBIT",
            "dailyLimit": 1000.0
        }
        """;

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.maskedCardNumber").value("411111******1111"))
                .andExpect(jsonPath("$.type").value("DEBIT"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getMyCards_Success() throws Exception {
        when(securityService.getCurrentUserId()).thenReturn(1L);

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<CardDto> cardPage = new PageImpl<>(Arrays.asList(cardDto), pageRequest, 1);

        when(cardService.getUserCardsPaginated(anyLong(), any(), any(Pageable.class))).thenReturn(cardPage);

        mockMvc.perform(get("/cards/my-cards")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("411111******1111"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCardById_Success() throws Exception {
        when(securityService.isAdmin()).thenReturn(true);
        doNothing().when(securityService).checkUserAccess(anyLong());
        when(cardService.getCardById(anyLong())).thenReturn(cardDto);

        mockMvc.perform(get("/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void updateCardStatus_UserBlock_Success() throws Exception {
        when(securityService.isAdmin()).thenReturn(false);
        doNothing().when(securityService).checkCardAccess(anyLong());

        when(cardService.updateCardStatus(anyLong(), eq(Card.CardStatus.BLOCKED))).thenReturn(blockedCard);
        when(cardService.convertToDto(any(Card.class))).thenReturn(blockedCardDto);

        mockMvc.perform(put("/cards/1/status")
                        .param("status", "BLOCKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void requestCardBlock_Success() throws Exception {
        doNothing().when(securityService).checkCardAccess(anyLong());

        when(cardService.updateCardStatus(anyLong(), eq(Card.CardStatus.BLOCKED))).thenReturn(blockedCard);
        when(cardService.convertToDto(any(Card.class))).thenReturn(blockedCardDto);

        mockMvc.perform(post("/cards/1/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getAccountCards_Success() throws Exception {
        doNothing().when(securityService).checkUserAccess(anyLong());
        List<CardDto> cards = Arrays.asList(cardDto);
        when(cardService.getAccountCards(anyLong())).thenReturn(cards);

        mockMvc.perform(get("/cards/account/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].accountId").value(1L));
    }
}