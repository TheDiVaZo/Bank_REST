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
        componentModel = MappingConstants.ComponentModel.SPRING,
        imports = {
                PasswordEncoder.class,
        },
        uses = {
                PasswordEncoder.class,
        },
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public abstract class UserMapper {

    private PasswordEncoder passwordEncoder;

    public abstract UserDto toDto(User user);

    @Mapping(target = "role", expression = "java(com.example.bankcards.entity.Role.USER)")
    @Mapping(target = "refreshTokenExpiry", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "password", expression = "java(passwordEncoder.encode(userRegistrationRequest.getPassword()))")
    public abstract User fromUserRegistrationRequest(UserRegistrationRequest userRegistrationRequest);
}
