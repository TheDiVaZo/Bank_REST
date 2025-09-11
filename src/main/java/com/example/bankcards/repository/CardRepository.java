package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    Optional<Card> findByCardNumber(String cardNumber);

    List<Card> findByUser_Id(UUID userId);
    List<Card> findByUser_PhoneNumber(String userPhoneNumber);

    Optional<Card> findByCardNumberAndUser_Id(String cardNumber, UUID userId);
    Optional<Card> findByCardNumberAndUser_PhoneNumber(String cardNumber, String userPhoneNumber);
}
