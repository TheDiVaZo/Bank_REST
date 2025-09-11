package com.example.bankcards.dto.card;

import com.example.bankcards.util.Patterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public final class CardCreateRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Номер обязателен")
    @Pattern(regexp = Patterns.PHONE_NUMBER, message = "Номер телефона записывается без +7. Пример: 9231234567")
    private String phoneNumber;

}
