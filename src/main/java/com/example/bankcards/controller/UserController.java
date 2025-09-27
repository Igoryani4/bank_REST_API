package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.SecurityService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final SecurityService securityService;



    @GetMapping("/{id}/with-accounts")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        if(securityService.isAdmin()){
            securityService.checkUserAccess(id);
        }
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/my-profile")
    public ResponseEntity<User> getMyProfile() {
        Long userId = securityService.getCurrentUserId();
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}/with-cards")
    public ResponseEntity<UserDto> getUserWithCards(@PathVariable Long id) {
        if(securityService.isAdmin()){
            securityService.checkUserAccess(id);
        }
        UserDto user = userService.getUserWithCards(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        securityService.checkAdminAccess();
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        securityService.checkAdminAccess();
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/with-accounts")
    public ResponseEntity<List<User>> getAllUsers() {
        securityService.checkAdminAccess();
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/with-cards")
    public ResponseEntity<List<UserDto>> getAllUsersWithCards() {
        securityService.checkAdminAccess();
        List<UserDto> users = userService.getAllUsersWithCards();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDto userUpdateDto) {
        securityService.checkAdminAccess();
        User updatedUser = userService.updateUser(id, userUpdateDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        securityService.checkAdminAccess();
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}