package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    // Поиск по зашифрованному номеру не поддерживается для безопасности
    // Optional<Card> findByCardNumber(String cardNumber);

    List<Card> findByAccountId(Long accountId);
    List<Card> findByAccountUserId(Long userId);

    @Query("SELECT c FROM Card c WHERE c.account.user.id = :userId")
    Page<Card> findByAccountUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(c) > 0 FROM Card c WHERE c.encryptedCardNumber = :encryptedCardNumber")
    boolean existsByCardNumber(@Param("encryptedCardNumber") String encryptedCardNumber);
}