package com.example.bankcards.entity;

import com.example.bankcards.service.EncryptionService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "encrypted_card_number", unique = true, nullable = false)
    private String encryptedCardNumber;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "encrypted_cvv", nullable = false)
    private String encryptedCvv;

    @Column(name = "card_holder_name", nullable = false)
    private String cardHolderName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    @Column(name = "daily_limit", precision = 15, scale = 2)
    private BigDecimal dailyLimit;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Transient
    @JsonIgnore
    private String decryptedCardNumber;

    @Transient
    @JsonIgnore
    private String decryptedCvv;

    @Setter
    @Transient
    @JsonIgnore
    private static EncryptionService encryptionService;


    public String getMaskedCardNumber() {
        try {
            String decrypted = getCardNumber(); // пробуем получить полный номер
            if (decrypted == null || decrypted.length() < 4) {
                return "**** **** **** ****";
            }
            // Маскируем: оставляем только последние 4 цифры
            String lastFour = decrypted.substring(decrypted.length() - 4);
            return "**** **** **** " + lastFour + "  full number from transfer: " + decrypted;
        } catch (Exception e) {
            // Если дешифровка не удалась, возвращаем полную маску
            return "**** **** **** ****";
        }
    }

    public String getCardNumber() {
        if (encryptedCardNumber == null) return null;
        try {
            return encryptionService.decrypt(encryptedCardNumber);
        } catch (Exception e) {
            return null;
        }
    }

    public void setCardNumber(String cardNumber) {
        this.decryptedCardNumber = cardNumber;
        if (encryptionService != null && cardNumber != null) {
            this.encryptedCardNumber = encryptionService.encrypt(cardNumber);
        }
    }


    public void setCvv(String cvv) {
        this.decryptedCvv = cvv;
        if (encryptionService != null && cvv != null) {
            this.encryptedCvv = encryptionService.encrypt(cvv);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = CardStatus.ACTIVE;
        }
        // Шифруем данные перед сохранением
        if (decryptedCardNumber != null) {
            setCardNumber(decryptedCardNumber);
        }
        if (decryptedCvv != null) {
            setCvv(decryptedCvv);
        }
    }

    public void setMaskedCardNumber(String s) {
        this.encryptedCardNumber = s;
    }

    public enum CardType {
        DEBIT, CREDIT
    }

    public enum CardStatus {
        ACTIVE, BLOCKED, EXPIRED, I_HAVE_DELETE_THIS_CARD
    }
}