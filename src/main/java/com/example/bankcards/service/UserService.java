package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserRegistrationDto;
import com.example.bankcards.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {
    User createUser(User user);
    User getUserById(Long id);
    User getUserByUsername(String username);
    User getUserByEmail(String email);
    List<User> getAllUsers();

    UserDto getUserDtoById(Long id);
    List<UserDto> getAllUserDtos();
    UserDto getUserWithCards(Long userId);
    List<UserDto> getAllUsersWithCards();
    UserDto convertToDto(User user);

    User updateUser(Long userId, User userDetails);
    void deleteUser(Long userId);
    User registerUser(UserRegistrationDto registrationDto);
}