package com.example.bankcards.dto.card;

import com.example.bankcards.util.Patterns;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public final class TransactionRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Номер карты обязателен")
    @Pattern(regexp = Patterns.NUMBER_CARD, message = "Поле должно содержать 16 цифры")
    private String fromCardNumber;

    @NotBlank(message = "Номер карты обязателен")
    @Pattern(regexp = Patterns.NUMBER_CARD, message = "Поле должно содержать 16 цифр")
    private String toCardNumber;

    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.01", message = "Минимальная сумма: 0.01")
    private BigDecimal amount;
}
