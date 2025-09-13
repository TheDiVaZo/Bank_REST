package com.example.bankcards.dto.user;

import com.example.bankcards.entity.User;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public abstract class UserMapper {

    public abstract UserDto toDto(User user);

    @Mapping(target = "refreshTokenExpiry", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", expression = "java(com.example.bankcards.entity.Role.USER)")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    public abstract User fromUserRegistrationRequest(UserRegistrationRequest userRegistrationRequest);
}
