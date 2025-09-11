package com.example.bankcards.dto.card;

import com.example.bankcards.entity.CardStatus;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public final class CardDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String cardNumber;

    private LocalDate expiryDate;

    private String cardHolder;

    private BigDecimal balance;

    private CardStatus status;
}
