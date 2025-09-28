package com.example.bankcards.controller;

import com.example.bankcards.dto.AccountDto;
import com.example.bankcards.entity.Account;
import com.example.bankcards.service.AccountService;
import com.example.bankcards.service.SecurityService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private SecurityService securityService;

    private Account account;
    private AccountDto accountDto;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1L);
        account.setAccountNumber("1234567890");
        account.setBalance(BigDecimal.valueOf(1000.0));
        account.setCurrency("USD");
        account.setType(Account.AccountType.CURRENT);
        account.setStatus(Account.AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());

        accountDto = new AccountDto();
        accountDto.setId(1L);
        accountDto.setAccountNumber("1234567890");
        accountDto.setBalance(BigDecimal.valueOf(1000.0));
        accountDto.setCurrency("USD");
        accountDto.setType(Account.AccountType.CURRENT);
        accountDto.setStatus(Account.AccountStatus.ACTIVE);
        accountDto.setCreatedAt(LocalDateTime.now());
        accountDto.setUserId(1L);
        accountDto.setUserFullName("John Doe");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAccount_AdminAccess_Success() throws Exception {
        doNothing().when(securityService).checkAdminAccess();
        when(accountService.createAccount(any(Account.class))).thenReturn(account);
        when(accountService.convertToDto(any(Account.class))).thenReturn(accountDto);

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(account)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.balance").value(1000.0))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.type").value("CURRENT"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getMyAccounts_Success() throws Exception {
        when(securityService.getCurrentUserId()).thenReturn(1L);
        List<AccountDto> accounts = Arrays.asList(accountDto);
        when(accountService.getUserAccounts(anyLong())).thenReturn(accounts);

        mockMvc.perform(get("/accounts/my-accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].accountNumber").value("1234567890"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAccountById_AdminAccess_Success() throws Exception {
        doNothing().when(securityService).checkAdminAccess();
        when(accountService.getAccountById(anyLong())).thenReturn(accountDto);

        mockMvc.perform(get("/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAccountStatus_AdminAccess_Success() throws Exception {
        doNothing().when(securityService).checkAdminAccess();
        when(accountService.updateAccountStatus(anyLong(), any(Account.AccountStatus.class))).thenReturn(account);
        when(accountService.convertToDto(any(Account.class))).thenReturn(accountDto);

        mockMvc.perform(put("/accounts/1/status")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAccount_AdminAccess_Success() throws Exception {
        doNothing().when(securityService).checkAdminAccess();
        doNothing().when(accountService).deleteAccount(anyLong());

        mockMvc.perform(delete("/accounts/1"))
                .andExpect(status().isOk());
    }
}