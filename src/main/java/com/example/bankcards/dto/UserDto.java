package com.example.bankcards.dto;

import com.example.bankcards.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private User.UserStatus status;
    private LocalDateTime createdAt;
    private List<AccountDto> accounts;
    private List<String> roles;
}
