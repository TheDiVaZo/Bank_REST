package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CardOperationRequest;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.dto.user.UserRegistrationRequest;
import com.example.bankcards.service.card.CardService;
import com.example.bankcards.service.user.UserService;
import com.example.bankcards.util.Patterns;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    private final CardService cardService;
    private final UserService userService;

    @PostMapping("/card")
    public CardDto createCard(
            @RequestBody @Valid CardCreateRequest request
    ) {
        return cardService.create(request.getUserId());
    }

    @PostMapping("/card/activate")
    public CardDto activateCard(
            @RequestBody @Valid CardOperationRequest request
    ) {
        return cardService.activeForPanLast4(request.getUserId(), request.getPanLast4());
    }

    @PostMapping("/card/block")
    public CardDto blockCard(
            @RequestBody @Valid CardOperationRequest request
    ) {
        return cardService.blockForPanLast4(request.getUserId(), request.getPanLast4());
    }

    @DeleteMapping("/card")
    public void deleteCard(
            @RequestBody @Valid CardOperationRequest request
    ) {
        cardService.deleteForPanLast4(request.getUserId(), request.getPanLast4());
    }

    @GetMapping("/cards")
    public Page<CardDto> getAllCards(
            @PageableDefault Pageable pageable
    ) {
        return cardService.getAll(pageable);
    }

    @GetMapping("/cards/{userId}")
    public Page<CardDto> getUserCards(
            @PathVariable UUID userId,
            @PageableDefault Pageable pageable
    ) {
        return cardService.getAllFromUser(userId, pageable);
    }

    @GetMapping("/users")
    public Page<UserDto> getAllUsers(
            @PageableDefault Pageable pageable
    ) {
        return userService.getAll(pageable);
    }

    @GetMapping("/user/{userId}")
    public UserDto getUserById(
            @PathVariable UUID userId) {
        return userService.getById(userId);
    }

    @GetMapping("/user/phone")
    public UserDto getUserByEmail(
            @RequestParam @Pattern(regexp = Patterns.PHONE_NUMBER) String phoneNumber
    ) {
        return userService.getByPhoneNumber(phoneNumber);
    }

    @PostMapping("/user")
    public UserDto createUser(@RequestBody @Valid UserRegistrationRequest request) {
        return userService.create(request);
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
