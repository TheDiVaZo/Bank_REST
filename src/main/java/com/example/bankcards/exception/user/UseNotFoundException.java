package com.example.bankcards.exception.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UseNotFoundException extends RuntimeException {
    public UseNotFoundException(String message) {
        super(message);
    }
}
