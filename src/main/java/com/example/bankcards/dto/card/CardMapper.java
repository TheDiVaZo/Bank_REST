package com.example.bankcards.dto.card;

import com.example.bankcards.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface CardMapper {

    CardDto toDto(Card card);
}
