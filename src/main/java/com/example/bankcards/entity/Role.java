package com.example.bankcards.entity;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER");

    private final String authTitle;

    Role(String authTitle) {
        this.authTitle = authTitle;
    }
}
