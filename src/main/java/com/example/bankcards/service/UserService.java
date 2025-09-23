package com.example.bankcards.service;

import com.example.bankcards.dto.UserRegistrationDto;
import com.example.bankcards.entity.User;

import java.util.List;

public interface UserService {
    User createUser(User user);
    User getUserById(Long id);
    User getUserByUsername(String username);
    User getUserByEmail(String email);
    List<User> getAllUsers();
    User updateUser(Long userId, User userDetails);
    void deleteUser(Long userId);
    User registerUser(UserRegistrationDto registrationDto);
}