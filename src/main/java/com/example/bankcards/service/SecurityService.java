package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public void checkCardAccess(Long cardId) {
        String currentUsername = getCurrentUsername();
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (!hasAdminRole() && !card.getAccount().getUser().getUsername().equals(currentUsername)) {
            log.warn("Access denied: User {} tried to access card {}", currentUsername, cardId);
            throw new AccessDeniedException("No access to this card");
        }
    }

    public Long getCurrentUserId() {
        String username = getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username))
                .getId();
    }

    public String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public boolean hasAdminRole() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public boolean isCurrentUser(Long userId) {
        return getCurrentUserId().equals(userId);
    }

    // Добавляем эти методы в существующий SecurityService
    public boolean isAdmin() {
        return hasAdminRole();
    }

    public void checkAdminAccess() {
        if (!isAdmin()) {
            throw new AccessDeniedException("Admin access required");
        }
    }

    public boolean canViewAllCards() {
        return isAdmin();
    }

    // Обновляем метод checkUserAccess для большей гибкости
    public void checkUserAccess(Long userId) {
        if (!hasAdminRole() && !getCurrentUserId().equals(userId)) {
            throw new AccessDeniedException("No access to this user data");
        }
    }
}