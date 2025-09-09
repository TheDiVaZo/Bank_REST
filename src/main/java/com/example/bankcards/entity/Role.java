package com.example.bankcards.entity;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN,
    USER;

    private final String authTitle = "ROLE_" + this.name();
}
