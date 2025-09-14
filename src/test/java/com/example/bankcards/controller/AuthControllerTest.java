package com.example.bankcards.controller;

import com.example.bankcards.dto.user.UserAuthResponse;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.dto.user.UserLoginRequest;
import com.example.bankcards.dto.user.UserRegistrationRequest;
import com.example.bankcards.exception.GlobalExceptionHandler;
import com.example.bankcards.service.auth.AuthServiceImpl;
import com.example.bankcards.service.cookie.CookieServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    @Mock
    private AuthServiceImpl authServiceImpl;
    @Mock
    private CookieServiceImpl cookieServiceImpl;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    private UserDto testUserDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void regUserAndReturnTokenAndUserDto() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Валера", "Газенбек", "9231234567", "password123"
        );
        UserAuthResponse mockResponse = new UserAuthResponse(
                "access", "refresh", new UserDto()
        );

        when(authServiceImpl.registrationUser(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/auth/reg")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access"))
                .andExpect(jsonPath("$.refresh_token").value("refresh"));

        verify(cookieServiceImpl).setRefreshToken(any(), eq("refresh"));
    }

    @Test
    void logUserAndReturnDtoAndSetCookies() throws Exception {
        UserLoginRequest request = new UserLoginRequest("9231234567", "password123");
        UserAuthResponse mockResponse = new UserAuthResponse(
                "access-token", "refresh-token", testUserDTO
        );

        when(authServiceImpl.loginUser(any(UserLoginRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access-token"))
                .andExpect(jsonPath("$.refresh_token").value("refresh-token"))
                .andExpect(jsonPath("$.user").value(testUserDTO));

        verify(cookieServiceImpl).setRefreshToken(any(), eq("refresh-token"));
    }
}
