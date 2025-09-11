package com.example.bankcards.dto.user;

import com.example.bankcards.entity.Role;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder(toBuilder = true)
public final class UserDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private Role role;

    private LocalDateTime createdAt;
}
