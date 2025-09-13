package com.example.bankcards.dto.user;

import com.example.bankcards.util.Patterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder(toBuilder = true)
public final class UserRegistrationRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Имя обязательно")
    private String firstName;

    @NotBlank(message = "Фамилия обязательно")
    private String lastName;

    @NotBlank(message = "Номер обязателен")
    @Pattern(regexp = Patterns.PHONE_NUMBER, message = "Номер телефона записывается без +7. Пример: 9231234567")
    private String phoneNumber;

    @Size(min = 4, max = 64)
    private String password;
}
