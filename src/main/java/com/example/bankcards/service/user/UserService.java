package com.example.bankcards.service.user;

import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.dto.user.UserRegistrationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    UserDto create(UserRegistrationRequest userDto);

    UserDto getById(UUID id);

    UserDto getByPhoneNumber(String phoneNumber);

    void delete(UUID userId);

    Page<UserDto> getAll(Pageable pageable);
}
