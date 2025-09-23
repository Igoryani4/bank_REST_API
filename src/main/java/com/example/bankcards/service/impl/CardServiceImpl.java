package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;

    @Override
    @Transactional
    public Card createCard(Card card) {
        try {
            // Генерация номера карты
            String cardNumber = generateCardNumber();
            card.setCardNumber(cardNumber);

            // Генерация CVV
            card.setCvv(generateCvv());

            // Установка срока действия (3 года)
            card.setExpiryDate(LocalDate.now().plusYears(3));

            Card savedCard = cardRepository.save(card);
            log.info("Created card: {} for account: {}", cardNumber, card.getAccount().getId());
            return savedCard;
        } catch (Exception e) {
            log.error("Error creating card for account: {}", card.getAccount().getId(), e);
            throw new RuntimeException("Failed to create card", e);
        }
    }

    @Override
    public CardDto getCardById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));
        return convertToDto(card);
    }

    @Override
    public CardDto getCardByNumber(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Card not found with number: " + cardNumber));
        return convertToDto(card);
    }

    @Override
    public List<CardDto> getAccountCards(Long accountId) {
        return cardRepository.findByAccountId(accountId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardDto> getUserCards(Long userId) {
        return cardRepository.findByAccountUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Card updateCardStatus(Long cardId, Card.CardStatus status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + cardId));

        card.setStatus(status);
        Card updatedCard = cardRepository.save(card);
        log.info("Updated card status: {} to {}", cardId, status);
        return updatedCard;
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + cardId));

        cardRepository.delete(card);
        log.info("Deleted card: {}", cardId);
    }

    @Override
    public CardDto convertToDto(Card card) {
        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setCardNumber(maskCardNumber(card.getCardNumber()));
        dto.setExpiryDate(card.getExpiryDate());
        dto.setCardHolderName(card.getCardHolderName());
        dto.setType(card.getType());
        dto.setStatus(card.getStatus());
        dto.setDailyLimit(card.getDailyLimit());
        dto.setCreatedAt(card.getCreatedAt());
        dto.setAccountId(card.getAccount().getId());
        dto.setAccountNumber(card.getAccount().getAccountNumber());
        return dto;
    }

    private String generateCardNumber() {
        Random random = new Random();
        String number;
        do {
            // Генерация 16-значного номера карты
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                sb.append(random.nextInt(10));
            }
            number = sb.toString();
        } while (cardRepository.existsByCardNumber(number));

        return number;
    }

    private String generateCvv() {
        Random random = new Random();
        return String.format("%03d", random.nextInt(1000));
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 12) {
            return cardNumber;
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}