package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByAccountId(Long accountId);
    List<Card> findByAccountUserId(Long userId);

    @Query("SELECT c FROM Card c WHERE c.account.user.id = :userId")
    Page<Card> findByAccountUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.account.user.id = :userId AND (:status IS NULL OR c.status = :status)")
    Page<Card> findByAccountUserIdAndStatus(@Param("userId") Long userId,
                                            @Param("status") Card.CardStatus status,
                                            Pageable pageable);

    // Для ADMIN - все карты с фильтрацией
    @Query("SELECT c FROM Card c WHERE (:userId IS NULL OR c.account.user.id = :userId) AND (:status IS NULL OR c.status = :status)")
    Page<Card> findAllWithFilters(@Param("userId") Long userId,
                                  @Param("status") Card.CardStatus status,
                                  Pageable pageable);

    @Query("SELECT COUNT(c) > 0 FROM Card c WHERE c.encryptedCardNumber = :encryptedCardNumber")
    boolean existsByCardNumber(@Param("encryptedCardNumber") String encryptedCardNumber);

    // Поиск просроченных карт для автоматического обновления статуса
    @Query("SELECT c FROM Card c WHERE c.expiryDate < CURRENT_DATE AND c.status = 'ACTIVE'")
    List<Card> findExpiredActiveCards();
}