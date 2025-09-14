package com.example.bankcards.dto.user;

import com.example.bankcards.entity.Role;
import com.example.bankcards.util.Patterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

import static com.example.bankcards.util.Patterns.NOT_BLANK_OR_NULL;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder(toBuilder = true)
public final class UserUpdateRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Pattern(regexp = NOT_BLANK_OR_NULL, message = "Поле не должно быть пустым или содержать только пробелы")
    private String firstName;

    @Pattern(regexp = NOT_BLANK_OR_NULL, message = "Поле не должно быть пустым или содержать только пробелы")
    private String lastName;

    @Pattern(regexp = Patterns.PHONE_NUMBER, message = "Номер телефона записывается без +7. Пример: 9231234567")
    private String phoneNumber;

    private Role role;
}
