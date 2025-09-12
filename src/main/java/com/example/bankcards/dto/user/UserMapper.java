package com.example.bankcards.dto.user;

import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.control.MappingControl;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        imports = {
                PasswordEncoder.class,
        },
        uses = {
                PasswordEncoder.class,
        }
)
public abstract class UserMapper {

    private final PasswordEncoder passwordEncoder;

    protected UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public abstract UserDto toDto(User user);

    @Mapping(target = "role", expression = "java(com.example.bankcards.entity.Role.USER)")
    @Mapping(target = "refreshTokenExpiry", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDate.now())")
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "password", expression = "java(passwordEncoder.encode(userRegistrationRequest.getPassword()))")
    public abstract User fromUserRegistrationRequest(UserRegistrationRequest userRegistrationRequest);
}
