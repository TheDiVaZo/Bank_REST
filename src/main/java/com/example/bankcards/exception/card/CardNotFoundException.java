package com.example.bankcards.exception.card;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
@Getter
public class CardNotFoundException extends RuntimeException {
    private final String cardNumber;
    private final String phoneNumber;

    public CardNotFoundException(String cardNumber, String phoneNumber) {
        super(String.format("Card '%s' not found for user '%s'", cardNumber, phoneNumber));
        this.cardNumber = cardNumber;
        this.phoneNumber = phoneNumber;
    }
}
