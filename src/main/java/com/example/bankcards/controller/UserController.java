package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.TransactionRequest;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.card.CardService;
import com.example.bankcards.service.user.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.bankcards.entity.Role.ROLE_USER;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('"+ROLE_USER+"')")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final CardService cardService;

    @GetMapping("/cards")
    @PreAuthorize("isAuthenticated()")
    public Page<CardDto> getUserCards(
            @AuthenticationPrincipal User user,
            @PageableDefault Pageable pageable
    ) {
        return cardService.getAllFromUser(user.getId(), pageable);
    }

    @PostMapping("/card/block")
    public void blockCard(
            @AuthenticationPrincipal User user,
            @RequestParam String panLast4
    ) {
        cardService.blockForPanLast4(user.getId(), panLast4);
    }

    @PostMapping("/transaction")
    public void transfer(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TransactionRequest request
    ) {
        cardService.transaction(user.getId(), request.getFromPanLast4(), user.getId(), request.getFromPanLast4(), request.getAmount());
    }

    @PostMapping("/profile")
    public UserDto getUser(
            @AuthenticationPrincipal User user
    ) {
        return userService.getById(user.getId());
    }

}
