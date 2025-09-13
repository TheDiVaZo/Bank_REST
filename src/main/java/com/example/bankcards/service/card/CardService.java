package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.CardDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface CardService {

    CardDto create(UUID userId);

    Page<CardDto> getAllFromUser(UUID userId, Pageable pageable);

    Page<CardDto> getAll(Pageable pageable);

    /* for pan last 4 */

    CardDto getByPanLast4(UUID userId, String panLast4);

    String getPan(UUID userId, String panLast4);

    CardDto blockForPanLast4(UUID userId, String panLast4);

    CardDto activeForPanLast4(UUID userId, String panLast4);

    void deleteForPanLast4(UUID userId, String panLast4);

    /* card transaction */

    void transaction(UUID fromUserId, String fromPanLast4, UUID toUserId, String toPanLast4, BigDecimal amount);
}
