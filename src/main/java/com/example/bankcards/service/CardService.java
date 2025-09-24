package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CardService {
    Card createCard(Card card);
    CardDto getCardById(Long id);
    CardDto getCardByNumber(String cardNumber);
    List<CardDto> getAccountCards(Long accountId);
    List<CardDto> getUserCards(Long userId);

    Page<CardDto> getUserCardsPaginated(Long userId, Pageable pageable);
    Card updateCardStatus(Long cardId, Card.CardStatus status);
    void deleteCard(Long cardId);
    CardDto convertToDto(Card card);
}