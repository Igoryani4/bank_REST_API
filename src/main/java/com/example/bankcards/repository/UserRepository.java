package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"accounts"})
    @Query("SELECT u FROM User u WHERE u.id = :userId")
    Optional<User> findByIdWithAccountsAndCards(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"accounts"})
    @Query("SELECT u FROM User u")
    List<User> findAllWithAccountsAndCards();
}