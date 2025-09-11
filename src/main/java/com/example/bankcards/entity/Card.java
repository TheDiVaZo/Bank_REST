package com.example.bankcards.entity;

import com.example.bankcards.util.Patterns;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "cards",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_card_fingerprint", columnNames = "fingerprint")
        },
        indexes = {
                @Index(name = "ix_cards_last4", columnList = "pan_last4"),
                @Index(name = "ix_cards_user_last4", columnList = "user_id, pan_last4")
        })
public class Card {

    /** Уникальный идентификатор сущности в базе. **/
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    /** Полный номер карты, состоящий из 16-ти цифр, зашифрованный системой шифрования AES-GCM. */
    @Column(name = "pan_encrypted", nullable = false, length = 512)
    private String panEncrypted;

    /** Последние 4 цифры карты. Уникально в паре с user_id. Данное поле индексируется для ускорения поиска и хранится в открытом виде */
    @Pattern(regexp = Patterns.NUMBER_4_CARD, message = "Требуется указать 4 последних цифры карты")
    @Column(name = "pan_last4", nullable = false, length = 4)
    private String panLast4;

    /** Полный номер карты зашифрованный HMAC-SHA256 в hex/base64 для быстрого сравнения номера карты с незашифрованным номером. Уникален глобально */
    @Column(name = "fingerprint", nullable = false, length = 64)
    private String fingerprint;

    /** Имя и Фамилия держателя карты на латинице. Пример: IVAN IVANOV **/
    @Column(name = "card_holder", nullable = false)
    private String cardHolder;

    /** Срок действия карты. В данном поле учитывается только месяц и год. День игнорируется  **/
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CardStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Маска для UI */
    public String maskedPan() {
        return "**** **** **** " + panLast4;
    }

    /** Нормализуем дату к первому дню месяца (на всякий случай). */
    @PrePersist @PreUpdate
    private void normalizeExpiry() {
        if (expiryDate != null) {
            this.expiryDate = expiryDate.withDayOfMonth(1);
        }
    }

}
