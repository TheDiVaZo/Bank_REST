package com.example.bankcards.dto.user;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public final class UserAuthResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String accessToken;

    private String refreshToken;

    private UserDto userDto;
}
