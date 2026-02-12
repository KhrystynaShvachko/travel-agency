package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.*;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationRestController Tests")
class AuthenticationRestControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationRestController authenticationRestController;

    private AuthResponse authResponse;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");

        authResponse = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();
    }

    @Test
    @DisplayName("signUp - Should register user and return auth response")
    void signUp_Success() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("newuser@example.com");

        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> result = authenticationRestController.signUp(registerRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("access-token", result.getBody().getAccessToken());
        assertEquals("refresh-token", result.getBody().getRefreshToken());
        verify(authenticationService).register(registerRequest);
    }

    @Test
    @DisplayName("signIn - Should login user and return auth response")
    void signIn_Success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> result = authenticationRestController.signIn(loginRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("access-token", result.getBody().getAccessToken());
        assertEquals("refresh-token", result.getBody().getRefreshToken());
        verify(authenticationService).login(loginRequest);
    }

    @Test
    @DisplayName("refreshToken - Should refresh tokens and return new auth response")
    void refreshToken_Success() {
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken("old-refresh-token");

        when(authenticationService.refresh(any(RefreshTokenRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> result = authenticationRestController.refreshToken(refreshRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("access-token", result.getBody().getAccessToken());
        assertEquals("refresh-token", result.getBody().getRefreshToken());
        verify(authenticationService).refresh(refreshRequest);
    }

    @Test
    @DisplayName("logout - Should logout user successfully")
    void logout_Success() {
        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setRefreshToken("refresh-token");

        ResponseEntity<Void> result = authenticationRestController.logout(logoutRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNull(result.getBody());
        verify(authenticationService).logout(logoutRequest);
    }


    @Test
    @DisplayName("validateToken - Should return valid token message")
    void validateToken_ReturnsValidMessage() {
        ResponseEntity<String> result = authenticationRestController.validateToken();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Token is valid", result.getBody());
    }

    @Test
    @DisplayName("resetPassword - Should reset password and return success message")
    void resetPassword_Success() {
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setNewPassword("newPassword123");

        ResponseEntity<String> result = authenticationRestController.resetPassword(resetRequest, testUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Password updated", result.getBody());
        verify(authenticationService).resetPassword(resetRequest, testUser);
    }
}
