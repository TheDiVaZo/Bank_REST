package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CardMapper;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ServiceErrorException;
import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.card.CardOperationException;
import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.crypto.CryptoService;
import com.example.bankcards.util.CardUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CryptoService crypto;
    private final CardMapper mapper;
    private final UserRepository userRepository;

    public CardServiceImpl(CardRepository cardRepository, CryptoService crypto, CardMapper mapper, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.crypto = crypto;
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    //TODO: нормально обработать ситуацию, когда сгенерированный рандомно номер уже есть в базе
    @Override
    @Transactional
    public CardDto create(UUID userId) {
        Objects.requireNonNull(userId, "userId");
        String pan = CardUtil.generateCardNumber();
        String last4 = pan.substring(pan.length() - 4);
        String enc = crypto.encrypt(pan);
        String fp  = crypto.fingerprint(pan);

        if (cardRepository.existsByFingerprint(fp)) {
            throw new ServiceErrorException("Card already exists (fingerprint). Retray please");
        }
        if (cardRepository.existsByUser_IdAndPanLast4(userId, last4)) {
            throw new ServiceErrorException("User already has a card with these last4. Retray please");
        }
        if (userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found");
        }

        try {
            User userRef = userRepository.getReferenceById(userId);
            Card.CardBuilder builder = Card.builder();
            builder.user(userRef);
            builder.panEncrypted(enc);
            builder.panLast4(last4);
            builder.fingerprint(fp);
            builder.cardHolder(
                    String.format("%s %s", userRef.getFirstName(), userRef.getLastName())
                            .toUpperCase()
            );
            builder.expiryDate(LocalDate.now().withDayOfMonth(1).plusYears(5));
            builder.balance(java.math.BigDecimal.ZERO);
            builder.status(CardStatus.ACTIVE);
            Card card = builder
                    .build();
            card = cardRepository.save(card);
            cardRepository.flush();
            return mapper.toDto(card);
        } catch (DataIntegrityViolationException exception) {
            throw new UserNotFoundException("User not found", exception);
        } catch (Exception exception) {
            throw new ServiceErrorException(exception.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> getAllFromUser(UUID userId, boolean isPanDecrypt, Pageable pageable) {
        return cardRepository.findAllByUser_Id(userId, pageable).map(entity -> {
            CardDto cardDto = mapper.toDto(entity);
            if (isPanDecrypt) {
                cardDto.setPan(crypto.decrypt(cardDto.getPan()));
            }
            return cardDto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> getAll(Pageable pageable) {
        return cardRepository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CardDto getByPanLast4(UUID userId, String panLast4) {
        Card card = findByUserAndLast4OrThrow(userId, panLast4);
        return mapper.toDto(card);
    }

    @Override
    public String getPan(UUID userId, String panLast4) {
        Optional<String> panEncrypted = cardRepository.findPanEncryptedByUserIdAndLast4(userId, panLast4);
        if (panEncrypted.isEmpty()) throw new CardNotFoundException("Card for '"+userId+"' and '"+panEncrypted+"' not found");
        return crypto.decrypt(panEncrypted.get());
    }

    @Override
    @Transactional
    public CardDto blockForPanLast4(UUID userId, String panLast4) {
        Card card = findByUserAndLast4OrThrow(userId, panLast4);
        if (card.getStatus() != CardStatus.BLOCKED) {
            card.setStatus(CardStatus.BLOCKED);
            card = cardRepository.save(card);
        }
        return mapper.toDto(card);
    }

    @Override
    @Transactional
    public CardDto activeForPanLast4(UUID userId, String panLast4) {
        Card card = findByUserAndLast4OrThrow(userId, panLast4);
        if (card.getStatus() != CardStatus.ACTIVE) {
            card.setStatus(CardStatus.ACTIVE);
            card = cardRepository.save(card);
        }
        return mapper.toDto(card);
    }

    @Override
    @Transactional
    public void deleteForPanLast4(UUID userId, String panLast4) {
        Card card = findByUserAndLast4OrThrow(userId, panLast4);
        cardRepository.delete(card);
    }

    @Override
    @Transactional
    public void transaction(UUID fromUserId, String fromPanLast4, UUID toUserId, String toPanLast4, BigDecimal amount) {
        try {
            validAmount(amount);

            Card fromCard = findByUserAndLast4OrThrow(fromUserId, fromPanLast4);
            Card toCard = findByUserAndLast4OrThrow(toUserId, toPanLast4);

            validTransaction(fromCard, toCard, amount);

            cardRepository.flush();
        } catch (Exception exception) {
            throw new CardOperationException(exception);
        }
    }

    public void transaction(Card fromCard, Card toCard, BigDecimal amount) {
        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));
        cardRepository.saveAll(List.of(fromCard, toCard));
    }

    private static void validAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CardOperationException("Amount must be positive");
        }
    }

    private static void validTransaction(Card fromCard, Card toCard, BigDecimal amount) {
        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardOperationException("One of the cards is not active");
        }
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new CardOperationException("Insufficient funds");
        }
        if (fromCard.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new CardOperationException("The card is not enough funds");
        }
    }

    /* Утилиты */

    private Card findByUserAndLast4OrThrow(UUID userId, String panLast4) {
        return cardRepository.findByUser_IdAndPanLast4(userId, panLast4)
                .orElseThrow(() -> new CardNotFoundException("Card not found by last4 for user"));
    }

    /**
     * Сейчас система не имеет API для поиска по полному номеру карты, т.к. этого нет в ТЗ.
     * Эта функция просто означает то, что API для работы с полным номером карты можно сделать в будущем
     * @param pan Полный четырех-значный номер карты
     * @return Объект карты или CardNotFoundException если такой нет.
     */
    private Card findByPanOrThrow(String pan) {
        String fp = crypto.fingerprint(pan);
        return cardRepository.findByFingerprint(fp)
                .orElseThrow(() -> new CardNotFoundException("Card not found by PAN"));
    }
}
