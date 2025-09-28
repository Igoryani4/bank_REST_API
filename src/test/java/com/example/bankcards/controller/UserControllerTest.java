package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.SecurityService;
import com.example.bankcards.service.UserService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private SecurityService securityService;

    private User user;
    private UserDto userDto;
    private UserUpdateDto userUpdateDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .status(User.UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("testuser");
        userDto.setEmail("test@example.com");
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setPhoneNumber("+1234567890");
        userDto.setStatus(User.UserStatus.ACTIVE);
        userDto.setCreatedAt(LocalDateTime.now());

        userUpdateDto = new UserUpdateDto();
        userUpdateDto.setFirstName("John Updated");
        userUpdateDto.setLastName("Doe Updated");
        userUpdateDto.setEmail("updated@example.com");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getMyProfile_Success() throws Exception {
        // Arrange
        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(userService.getUserById(anyLong())).thenReturn(user);

        // Act & Assert
        mockMvc.perform(get("/users/my-profile")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getUserById_AdminAccess_Success() throws Exception {
        // Arrange
        when(securityService.isAdmin()).thenReturn(true);
        doNothing().when(securityService).checkUserAccess(anyLong());
        when(userService.getUserById(anyLong())).thenReturn(user);

        // Act & Assert
        mockMvc.perform(get("/users/1/with-accounts")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getUserWithCards_Success() throws Exception {
        // Arrange
        when(securityService.isAdmin()).thenReturn(true);
        doNothing().when(securityService).checkUserAccess(anyLong());
        when(userService.getUserWithCards(anyLong())).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(get("/users/1/with-cards")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateUser_AdminAccess_Success() throws Exception {
        // Arrange
        User updatedUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("updated@example.com")
                .firstName("John Updated")
                .lastName("Doe Updated")
                .build();

        doNothing().when(securityService).checkAdminAccess();
        when(userService.updateUser(anyLong(), any(UserUpdateDto.class))).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllUsersWithCards_AdminAccess_Success() throws Exception {
        // Arrange
        doNothing().when(securityService).checkAdminAccess();
        List<UserDto> users = Arrays.asList(userDto);
        when(userService.getAllUsersWithCards()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/users/with-cards")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteUser_AdminAccess_Success() throws Exception {
        // Arrange
        doNothing().when(securityService).checkAdminAccess();
        doNothing().when(userService).deleteUser(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/users/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

}