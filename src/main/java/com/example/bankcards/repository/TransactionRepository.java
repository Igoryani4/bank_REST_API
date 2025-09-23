package com.example.bankcards.repository;

import com.example.bankcards.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromAccountIdOrToAccountId(Long fromAccountId, Long toAccountId);

    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.user.id = :userId OR t.toAccount.user.id = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findByUserId(@Param("userId") Long userId);

    List<Transaction> findByFromAccountAccountNumberOrToAccountAccountNumber(String fromAccount, String toAccount);

    @Query("SELECT t FROM Transaction t WHERE (t.fromAccount.user.id = :userId OR t.toAccount.user.id = :userId) AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findByUserIdAndDateRange(@Param("userId") Long userId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
}
