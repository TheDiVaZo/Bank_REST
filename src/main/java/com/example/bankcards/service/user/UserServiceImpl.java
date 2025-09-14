package com.example.bankcards.service.user;

import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.dto.user.UserMapper;
import com.example.bankcards.dto.user.UserRegistrationRequest;
import com.example.bankcards.dto.user.UserUpdateRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.user.UserExistException;
import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserDto create(UserRegistrationRequest userDto) {
        if (userRepository.existsByPhoneNumber(userDto.getPhoneNumber())) throw new UserExistException("User is already registered with number");

        User user = userMapper.fromUserRegistrationRequest(userDto);

        User savedUser = userRepository.save(user);
        userRepository.flush();
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getById(UUID id) {
        return userRepository.findById(id).map(userMapper::toDto).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).map(userMapper::toDto).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public void delete(UUID userId) {
        if (!userRepository.existsById(userId)) throw new UserNotFoundException("User not found");
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public UserDto update(UUID userId, UserUpdateRequest userUpdateRequest) {
        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
            if (userUpdateRequest.getFirstName() != null) user.setFirstName(userUpdateRequest.getFirstName());
            if (userUpdateRequest.getLastName() != null) user.setLastName(userUpdateRequest.getLastName());
            if (userUpdateRequest.getPhoneNumber() != null) user.setPhoneNumber(userUpdateRequest.getPhoneNumber());
            if (userUpdateRequest.getRole() != null) user.setRole(userUpdateRequest.getRole());
            userRepository.flush();
            return userMapper.toDto(user);
        } catch (DataIntegrityViolationException exception) {
            throw new UserExistException("User is already registered with number");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }
}
