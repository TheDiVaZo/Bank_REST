package com.example.bankcards.service;

import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.dto.user.UserMapper;
import com.example.bankcards.dto.user.UserRegistrationRequest;
import com.example.bankcards.dto.user.UserUpdateRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.user.UserExistException;
import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.user.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private User existingUser;
    private UserDto existingUserDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        existingUser = new User();
        existingUser.setId(userId);
        existingUser.setPhoneNumber("1234567890");
        existingUser.setFirstName("Валера");
        existingUser.setLastName("Газенбек");
        existingUser.setRole(Role.USER);

        existingUserDto = new UserDto();
        existingUserDto.setId(userId);
        existingUserDto.setPhoneNumber(existingUser.getPhoneNumber());
        existingUserDto.setFirstName(existingUser.getFirstName());
        existingUserDto.setLastName(existingUser.getLastName());
        existingUserDto.setRole(existingUser.getRole());
    }

    @Test
    void create_success() {
        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setPhoneNumber("1111111111");
        req.setFirstName("Alice");
        req.setLastName("Smith");

        User mapped = new User();
        mapped.setId(UUID.randomUUID());
        mapped.setPhoneNumber(req.getPhoneNumber());
        mapped.setFirstName(req.getFirstName());
        mapped.setLastName(req.getLastName());

        UserDto dto = new UserDto();
        dto.setId(mapped.getId());
        dto.setPhoneNumber(mapped.getPhoneNumber());
        dto.setFirstName(mapped.getFirstName());
        dto.setLastName(mapped.getLastName());

        when(userRepository.existsByPhoneNumber(req.getPhoneNumber())).thenReturn(false);
        when(userMapper.fromUserRegistrationRequest(req)).thenReturn(mapped);
        when(userRepository.save(mapped)).thenReturn(mapped);
        when(userMapper.toDto(mapped)).thenReturn(dto);

        UserDto result = userService.create(req);

        assertThat(result).isEqualTo(dto);
        verify(userRepository).existsByPhoneNumber(req.getPhoneNumber());
        verify(userMapper).fromUserRegistrationRequest(req);
        verify(userRepository).save(mapped);
        verify(userMapper).toDto(mapped);
    }

    @Test
    void create_phoneExists_throws() {
        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setPhoneNumber("1234567890");

        when(userRepository.existsByPhoneNumber(req.getPhoneNumber())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(req))
                .isInstanceOf(UserExistException.class)
                .hasMessageContaining("already registered");

        verify(userRepository).existsByPhoneNumber(req.getPhoneNumber());
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    void getById_found() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userMapper.toDto(existingUser)).thenReturn(existingUserDto);

        UserDto result = userService.getById(userId);

        assertThat(result).isEqualTo(existingUserDto);
        verify(userRepository).findById(userId);
        verify(userMapper).toDto(existingUser);
    }

    @Test
    void getById_notFound_throws() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(userId);
        verifyNoInteractions(userMapper);
    }

    @Test
    void getByPhone_found() {
        String phone = existingUser.getPhoneNumber();

        when(userRepository.findByPhoneNumber(phone)).thenReturn(Optional.of(existingUser));
        when(userMapper.toDto(existingUser)).thenReturn(existingUserDto);

        UserDto result = userService.getByPhoneNumber(phone);

        assertThat(result).isEqualTo(existingUserDto);
        verify(userRepository).findByPhoneNumber(phone);
        verify(userMapper).toDto(existingUser);
    }

    @Test
    void getByPhone_notFound_throws() {
        String phone = "0000000000";
        when(userRepository.findByPhoneNumber(phone)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByPhoneNumber(phone))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findByPhoneNumber(phone);
        verifyNoInteractions(userMapper);
    }

    @Test
    void delete_success() {
        doNothing().when(userRepository).deleteById(userId);
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.delete(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void delete_notFound_throws() {
        when(userRepository.existsById(userId)).thenReturn(false);
        assertThatThrownBy(() -> userService.delete(userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(userId);
    }

    @Test
    void update_success() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setFirstName("Jane");
        req.setLastName("Roe");
        req.setPhoneNumber("+2222222222"); // если номер меняется

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        // toDto расчитываем от переданного User, чтобы убедиться, что поля обновились
        when(userMapper.toDto(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            UserDto dto = new UserDto();
            dto.setId(u.getId());
            dto.setFirstName(u.getFirstName());
            dto.setLastName(u.getLastName());
            dto.setPhoneNumber(u.getPhoneNumber());
            return dto;
        });

        UserDto result = userService.update(userId, req);

        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Roe");
        assertThat(result.getPhoneNumber()).isEqualTo("+2222222222");

        verify(userRepository).findById(userId);
        verify(userRepository).flush();
        verify(userMapper).toDto(existingUser);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    void update_uniqueViolation_throwsUserExist() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setPhoneNumber("+duplicated");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(userRepository).flush();

        assertThatThrownBy(() -> userService.update(userId, req))
                .isInstanceOf(UserExistException.class);

        verify(userRepository).findById(userId);
        verify(userRepository).flush();
        // mapper не должен вызываться при исключении
        verifyNoInteractions(userMapper);
    }

    @Test
    void update_notFound_throws() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(userId, new UserUpdateRequest()))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(userId);
        verify(userRepository, never()).flush();
        verifyNoInteractions(userMapper);
    }

    @Test
    void getAll_success() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").descending());

        User u1 = new User();
        u1.setId(UUID.randomUUID());
        u1.setFirstName("A");
        User u2 = new User();
        u2.setId(UUID.randomUUID());
        u2.setFirstName("B");
        Page<User> page = new PageImpl<>(List.of(u1, u2), pageable, 2);

        UserDto d1 = new UserDto();
        d1.setId(u1.getId());
        d1.setFirstName("A");
        UserDto d2 = new UserDto();
        d2.setId(u2.getId());
        d2.setFirstName("B");

        when(userRepository.findAll(pageable)).thenReturn(page);
        when(userMapper.toDto(u1)).thenReturn(d1);
        when(userMapper.toDto(u2)).thenReturn(d2);

        Page<UserDto> result = userService.getAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(d1, d2);

        verify(userRepository).findAll(pageable);
        verify(userMapper).toDto(u1);
        verify(userMapper).toDto(u2);
    }
}

