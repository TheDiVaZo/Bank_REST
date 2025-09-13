package com.example.bankcards.dto.card;

import com.example.bankcards.util.Patterns;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = Patterns.NUMBER_CARD, message = "Поле должно содержать 4 последних цифры карты")
    private String fromPanLast4;

    @NotBlank(message = "Номер карты обязателен")
    @Pattern(regexp = Patterns.NUMBER_CARD, message = "Поле должно содержать 4 последних цифры карты")
    private String toPanLast4;

    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.01", message = "Минимальная сумма: 0.01")
    private BigDecimal amount;
}
