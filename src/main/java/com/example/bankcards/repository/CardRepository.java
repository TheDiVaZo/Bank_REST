package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID>, JpaSpecificationExecutor<Card> {
    @NotNull Optional<Card> findById(@NotNull UUID id);

    Optional<Card> findByUser_IdAndPanLast4(UUID userId, String panLast4);

    boolean existsByUser_IdAndPanLast4(UUID userId, String panLast4);

    Optional<Card> findByFingerprint(String fingerprint);

    boolean existsByFingerprint(String fingerprint);

    Page<Card> findAllByUser_Id(UUID userId, Pageable pageable);

    @Query("""
           select c.panEncrypted
           from Card c
           where c.user.id = :userId and c.panLast4 = :last4
           """)
    Optional<String> findPanEncryptedByUserIdAndLast4(UUID userId, String last4);
}
