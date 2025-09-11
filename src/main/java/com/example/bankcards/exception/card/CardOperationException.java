package com.example.bankcards.exception.card;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@Getter
public class CardOperationException extends RuntimeException {
    private final String cardNumber;

    public CardOperationException(String cardNumber, String message) {
        super(String.format("Invalid operation for card '%s': %s", cardNumber, message));
        this.cardNumber = cardNumber;
    }
}
