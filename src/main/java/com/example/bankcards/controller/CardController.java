package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.SecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final SecurityService securityService;

    @PostMapping
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody Card card) {
        Card createdCard = cardService.createCard(card);
        return ResponseEntity.ok(cardService.convertToDto(createdCard));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCardById(@PathVariable Long id) {
        CardDto card = cardService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    @GetMapping("/my-cards")
    public ResponseEntity<Page<CardDto>> getMyCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = securityService.getCurrentUserId();
        Page<CardDto> cards = cardService.getUserCardsPaginated(userId, PageRequest.of(page, size));
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
        Card updatedCard = cardService.updateCardStatus(id, status);
        return ResponseEntity.ok(cardService.convertToDto(updatedCard));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.ok().build();
    }
}
