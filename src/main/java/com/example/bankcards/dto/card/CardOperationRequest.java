package com.example.bankcards.dto.card;

import com.example.bankcards.util.Patterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public final class CardOperationRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Номер обязателен")
    @Pattern(regexp = Patterns.PHONE_NUMBER, message = "Номер телефона записывается без +7. Пример: 9231234567")
    private String phoneNumber;

    @NotBlank(message = "Номер карты обязателен")
    @Pattern(regexp = Patterns.NUMBER_CARD, message = "Поле должно содержать 16 цифр")
    private String cardNumber;
}
