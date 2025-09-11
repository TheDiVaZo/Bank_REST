package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<Card> {
    Optional<User> findByPhoneNumber(String phoneNumber);

    @NotNull Optional<User> findById(@NotNull UUID id);

    Optional<User> findByRefreshToken(String refreshToken);

    boolean existsByRefreshToken(String refreshToken);

    boolean existsByPhoneNumber(String phoneNumber);
}
