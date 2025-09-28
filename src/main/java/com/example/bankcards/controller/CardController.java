package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.SecurityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final CardService cardService;
    private final SecurityService securityService;

    @PostMapping
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody Card card) {
        securityService.checkAdminAccess();
        if (card.getAccount() == null) {
            return ResponseEntity.badRequest().build();
        }
        Card createdCard = cardService.createCard(card);
        return ResponseEntity.ok(cardService.convertToDto(createdCard));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCardById(@PathVariable Long id) {
        if (securityService.isAdmin()) {
            securityService.checkUserAccess(id);
        }
        CardDto card = cardService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    @GetMapping("/my-cards")
    public ResponseEntity<Page<CardDto>> getMyCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Card.CardStatus status) {

        Long userId = securityService.getCurrentUserId();
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy));

        Page<CardDto> cards = cardService.getUserCardsPaginated(userId, status, pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/admin/all-cards")
    public ResponseEntity<Page<CardDto>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Card.CardStatus status,
            @RequestParam(required = false) Long userId) {
        securityService.checkAdminAccess();
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy));

        Page<CardDto> cards = cardService.getAllCardsPaginated(userId, status, pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<CardDto>> getAccountCards(@PathVariable Long accountId) {
        securityService.checkUserAccess(accountId);
        List<CardDto> cards = cardService.getAccountCards(accountId);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CardDto>> getUserCards(@PathVariable Long userId) {
        securityService.checkUserAccess(userId);
        List<CardDto> cards = cardService.getUserCards(userId);
        return ResponseEntity.ok(cards);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CardDto> updateCardStatus(
            @PathVariable Long id,
            @RequestParam Card.CardStatus status) {

        if (!securityService.isAdmin()) {
            securityService.checkCardAccess(id);
            if (status != Card.CardStatus.BLOCKED) {
                throw new RuntimeException("Users can only request card blocking");
            }
        }

        Card updatedCard = cardService.updateCardStatus(id, status);
        return ResponseEntity.ok(cardService.convertToDto(updatedCard));
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<CardDto> requestCardBlock(@PathVariable Long id) {
        securityService.checkCardAccess(id);
        Card updatedCard = cardService.updateCardStatus(id, Card.CardStatus.BLOCKED);
        return ResponseEntity.ok(cardService.convertToDto(updatedCard));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        securityService.checkAdminAccess();
        cardService.deleteCard(id);
        return ResponseEntity.ok().build();
    }
}