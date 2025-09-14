package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CardMapper;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.crypto.CryptoService;
import com.example.bankcards.service.card.CardServiceImpl;
import com.example.bankcards.util.CardUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardMapper mapper;
    @InjectMocks
    private CardServiceImpl cardService;
    @Mock
    private CryptoService crypto;
    @Captor
    private ArgumentCaptor<Card> cardCaptor;

    private UUID testUserId;
    private User testUser;
    private Card testCard;
    private String testPanLast4;
    private String testEncryptedPan;
    private String testFingerprint;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testPanLast4 = "1234";
        testEncryptedPan = crypto.encrypt("1234123412341234");
        testFingerprint = crypto.fingerprint("1234123412341234");

        testUser = new User(
                testUserId, "9231234567", "Валера",
                "Газенбек",
                "password", Role.USER,
                LocalDateTime.now(), null, null
        );

        testCard = new Card();
        testCard.setPanLast4(testPanLast4);
        testCard.setPanEncrypted(testEncryptedPan);
        testCard.setFingerprint(testFingerprint);
        testCard.setCardHolder("ВАЛЕРА ГАЗЕНБЕК");
        testCard.setUser(testUser);
        testCard.setBalance(new BigDecimal("1000.00"));
        testCard.setStatus(CardStatus.ACTIVE);
    }

    @Test
    void create_success_firstTry() {
        String pan = "5555666677778888";
        CardDto dto = new CardDto();
        dto.setPanLast4("8888");
        dto.setCardHolder("ВАЛЕРА ГАЗЕНБЕК");

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cardRepository.existsByUser_IdAndPanLast4(testUserId, "8888")).thenReturn(false);
        when(crypto.encrypt(pan)).thenReturn("enc-" + pan);
        when(crypto.fingerprint(pan)).thenReturn("fp-" + pan);
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDto(any(Card.class))).thenReturn(dto);

        try (MockedStatic<CardUtil> mocked = Mockito.mockStatic(CardUtil.class)) {
            mocked.when(CardUtil::generateCardNumber).thenReturn(pan);

            // when
            CardDto result = cardService.create(testUserId);

            // then
            assertThat(result).isSameAs(dto);

            verify(cardRepository).save(cardCaptor.capture());
            Card saved = cardCaptor.getValue();

            assertThat(saved.getUser()).isEqualTo(testUser);
            assertThat(saved.getPanLast4()).isEqualTo("8888");
            assertThat(saved.getPanEncrypted()).isEqualTo("enc-" + pan);
            assertThat(saved.getFingerprint()).isEqualTo("fp-" + pan);
            assertThat(saved.getCardHolder()).isEqualTo("ВАЛЕРА ГАЗЕНБЕК");
            assertThat(saved.getStatus()).isEqualTo(CardStatus.ACTIVE);
            assertThat(saved.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Test
    void getByPanLast4_success() {
        UUID userId = UUID.randomUUID();
        String last4 = "1234";

        Card card = new Card();
        card.setPanLast4(last4);

        CardDto dto = new CardDto();
        dto.setPanLast4(last4);

        when(cardRepository.findByUser_IdAndPanLast4(userId, last4)).thenReturn(Optional.of(card));
        when(mapper.toDto(card)).thenReturn(dto);

        CardDto result = cardService.getByPanLast4(userId, last4);

        assertThat(result).isSameAs(dto);
        verify(cardRepository).findByUser_IdAndPanLast4(userId, last4);
        verify(mapper).toDto(card);
        verifyNoMoreInteractions(cardRepository, mapper);
    }

    @Test
    void getByPanLast4_cardNotFound_throws() {
        UUID userId = UUID.randomUUID();
        String last4 = "9999";

        when(cardRepository.findByUser_IdAndPanLast4(userId, last4)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getByPanLast4(userId, last4))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessageContaining("Card not found");

        verify(cardRepository).findByUser_IdAndPanLast4(userId, last4);
        verifyNoInteractions(mapper);
    }

}
