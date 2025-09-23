package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;

import java.util.List;

public interface CardService {
    Card createCard(Card card);
    CardDto getCardById(Long id);
    CardDto getCardByNumber(String cardNumber);
    List<CardDto> getAccountCards(Long accountId);
    List<CardDto> getUserCards(Long userId);
    Card updateCardStatus(Long cardId, Card.CardStatus status);
    void deleteCard(Long cardId);
    CardDto convertToDto(Card card);
}