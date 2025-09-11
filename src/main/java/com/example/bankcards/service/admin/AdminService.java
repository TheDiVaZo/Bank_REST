package com.example.bankcards.service.admin;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.user.UserDto;

import java.util.List;
import java.util.UUID;

public interface AdminService {

    /* CARD */

    CardDto createCard(String phoneNumber);

    CardDto blockCard(String cardNumber);

    CardDto activeCard(String cardNumber);

    void deleteCard(String cardNumber);

    List<CardDto> getUserCards(String phoneNumber);

    CardDto getCard(String cardNumber);

    List<CardDto> getAllCards();

    /* USER */

    UserDto createUser(UserDto userDto);

    void deleteUser(String phoneNumber);

    UserDto getUserByPhoneNumber(String phoneNumber);

    UserDto getUserById(UUID id);

    List<UserDto> getAllUsers();
}
