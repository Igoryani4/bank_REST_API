package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.EncryptionService;
import com.example.bankcards.service.SecurityService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
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
    private final SecurityService securityService;
    private final EncryptionService encryptionService;

    @PostConstruct
    public void init() {
        Card.setEncryptionService(encryptionService);
    }

    @Override
    @Transactional
    public Card createCard(Card card) {
        try {
            securityService.checkUserAccess(card.getAccount().getUser().getId());

            // Генерация номера карты
            String cardNumber = generateCardNumber();
            card.setCardNumber(cardNumber);

            // Генерация CVV
            card.setCvv(generateCvv());

            // Установка срока действия (3 года)
            card.setExpiryDate(LocalDate.now().plusYears(3));

            Card savedCard = cardRepository.save(card);
            log.info("Created card: {} for account: {}", maskCardNumber(cardNumber), card.getAccount().getId());
            return savedCard;
        } catch (Exception e) {
            log.error("Error creating card for account: {}", card.getAccount().getId(), e);
            throw new RuntimeException("Failed to create card", e);
        }
    }

    @Override
    public CardDto getCardById(Long id) {
        securityService.checkCardAccess(id);
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));
        return convertToDto(card);
    }

    @Override
    public CardDto getCardByNumber(String cardNumber) {
        // Поиск по зашифрованному номеру требует особой логики
        throw new UnsupportedOperationException("Search by card number not supported for security reasons");
    }

    @Override
    public List<CardDto> getAccountCards(Long accountId) {
        securityService.checkUserAccess(accountId); // Упрощенная проверка
        return cardRepository.findByAccountId(accountId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardDto> getUserCards(Long userId) {
        securityService.checkUserAccess(userId);
        return cardRepository.findByAccountUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Page<CardDto> getUserCardsPaginated(Long userId, Pageable pageable) {
        securityService.checkUserAccess(userId);
        return cardRepository.findByAccountUserId(userId, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public Card updateCardStatus(Long cardId, Card.CardStatus status) {
        securityService.checkCardAccess(cardId);
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
        securityService.checkCardAccess(cardId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + cardId));

        cardRepository.delete(card);
        log.info("Deleted card: {}", cardId);
    }

    @Override
    public CardDto convertToDto(Card card) {
        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setMaskedCardNumber(card.getMaskedCardNumber());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setCardHolderName(card.getCardHolderName());
        dto.setType(card.getType());
        dto.setStatus(card.getStatus());
        dto.setDailyLimit(card.getDailyLimit());
        dto.setCreatedAt(card.getCreatedAt());
        dto.setAccountId(card.getAccount().getId());
        dto.setAccountNumber(card.getAccount().getAccountNumber());
        dto.setBalance(card.getAccount().getBalance());
        return dto;
    }

    private String generateCardNumber() {
        Random random = new Random();
        String number;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                sb.append(random.nextInt(10));
            }
            number = sb.toString();
        } while (cardRepository.existsByCardNumber(encryptionService.encrypt(number)));

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