package com.example.bankcards.dto.card;

import com.example.bankcards.util.Patterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public final class CardOperationRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Юзер ID обязателен")
    private UUID userId;

    @NotBlank(message = "Номер карты обязателен")
    @Pattern(regexp = Patterns.NUMBER_4_CARD, message = "Поле должно содержать 4 последние цифры карты")
    private String panLast4;
}
