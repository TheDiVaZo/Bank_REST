package com.example.bankcards.exception.card;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@Getter
public class CardOperationException extends RuntimeException {
    public CardOperationException(String message) {
        super(String.format(message));
    }

    public CardOperationException(Throwable cause) {
        super(cause);
    }
}
