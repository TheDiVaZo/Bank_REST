package com.example.bankcards.dto.card;

import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.view.View;
import com.fasterxml.jackson.annotation.JsonView;
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

    @JsonView({View.Owner.class})
    private String pan;

    @JsonView({View.Owner.class, View.Admin.class})
    private String panLast4;

    private LocalDate expiryDate;

    private String cardHolder;

    private BigDecimal balance;

    private CardStatus status;
}
