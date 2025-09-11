package com.example.bankcards.dto.card;

import com.example.bankcards.entity.Card;
import com.example.bankcards.service.crypto.CryptoService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        imports = {
                CryptoService.class,
        },
        uses = {
                CryptoService.class,
        }
)
public abstract class CardMapper {

    private final CryptoService cryptoService;

    protected CardMapper(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @Mapping(target = "pan", ignore = true)
    public abstract CardDto toDtoAndNoEncrypt(Card card);

    @Mapping(target = "pan", expression = "java(cryptoService.decrypt(card.getPanEncrypted()))")
    public abstract CardDto toDtoAndEncrypt(Card card);
}
