package com.example.bankcards.service.impl;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.AccountService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final CardService cardService;
    private final AccountService accountService;




    @Override
    @Transactional
    public User createUser(User user) {
        try {
            if (Boolean.TRUE.equals(userRepository.existsByUsername(user.getUsername()))) {
                throw new IllegalArgumentException("Username already exists1: " + user.getUsername());
            }

            if (Boolean.TRUE.equals(userRepository.existsByEmail(user.getEmail()))) {
                throw new IllegalArgumentException("Email already exists1: " + user.getEmail());
            }

            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                user.setRoles(List.of("ROLE_USER"));
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));

            User savedUser = userRepository.save(user);
            log.info("Created user: {}", user.getUsername());
            return savedUser;
        } catch (Exception e) {
            log.error("Error creating user: {}", user.getUsername(), e);
            throw new IllegalArgumentException("Failed to create user", e);
        }
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserWithCards(Long userId) {
        User user = userRepository.findByIdWithAccountsAndCards(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return convertToDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAllUsersWithCards() {
        List<User> users = userRepository.findAllWithAccountsAndCards();
        return users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setRoles(user.getRoles());

        if (user.getAccounts() != null) {
            dto.setAccounts(user.getAccounts().stream()
                    .map(account -> {
                        AccountDto accountDto = accountService.convertToDto(account);
                        List<CardDto> cards = cardService.getAccountCards(account.getId());
                        accountDto.setCards(cards);
                        return accountDto;
                    })
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserDtoById(Long id) {
        User user = getUserById(id);
        return convertToDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAllUserDtos() {
        List<User> users = getAllUsers();
        return users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public User updateUser(Long userId, User userDetails) {
        User user = getUserById(userId);

        if (!user.getUsername().equals(userDetails.getUsername()) &&
                Boolean.TRUE.equals(userRepository.existsByUsername(userDetails.getUsername()))) {
            throw new IllegalArgumentException("Username already exists: " + userDetails.getUsername());
        }

        if (!user.getEmail().equals(userDetails.getEmail()) &&
                Boolean.TRUE.equals(userRepository.existsByEmail(userDetails.getEmail()))) {
            throw new IllegalArgumentException("Email already exists: " + userDetails.getEmail());
        }

        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setPhoneNumber(userDetails.getPhoneNumber());

        User updatedUser = userRepository.save(user);
        log.info("Updated user: {}", userId);
        return updatedUser;
    }

    @Override
    @Transactional
    public User updateUser(Long userId, UserUpdateDto userUpdateDto) {
        User user = getUserById(userId);

        if (userUpdateDto.hasUsername() && !user.getUsername().equals(userUpdateDto.getUsername())) {
            if (Boolean.TRUE.equals(userRepository.existsByUsername(userUpdateDto.getUsername()))) {
                throw new IllegalArgumentException("Username already exists: " + userUpdateDto.getUsername());
            }
            user.setUsername(userUpdateDto.getUsername());
        }

        if (userUpdateDto.hasEmail() && !user.getEmail().equals(userUpdateDto.getEmail())) {
            if (Boolean.TRUE.equals(userRepository.existsByEmail(userUpdateDto.getEmail()))) {
                throw new IllegalArgumentException("Email already exists: " + userUpdateDto.getEmail());
            }
            user.setEmail(userUpdateDto.getEmail());
        }

        if (userUpdateDto.hasPassword()) {
            user.setPassword(passwordEncoder.encode(userUpdateDto.getPassword()));
        }

        if (userUpdateDto.hasFirstName()) {
            user.setFirstName(userUpdateDto.getFirstName());
        }

        if (userUpdateDto.hasLastName()) {
            user.setLastName(userUpdateDto.getLastName());
        }

        if (userUpdateDto.hasRoles()) {
            user.setRoles(userUpdateDto.getRoles());
        }

        User updatedUser = userRepository.save(user);
        log.info("Updated user with partial data: {}", userId);
        return updatedUser;
    }


    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
        log.info("Deleted user: {}", userId);
    }

    @Override
    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        User user = User.builder()
                .username(registrationDto.getUsername())
                .email(registrationDto.getEmail())
                .password(registrationDto.getPassword())
                .firstName(registrationDto.getFirstName())
                .lastName(registrationDto.getLastName())
                .phoneNumber(registrationDto.getPhoneNumber())
                .build();

        return createUser(user);
    }
}