package com.epam.finaltask.service;

import com.epam.finaltask.model.User;
import com.epam.finaltask.service.impl.SecurityService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityService Tests")
class SecurityServiceTest {

    @InjectMocks
    private SecurityService securityService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("isUserObject - Should return true when user matches")
    void isUserObject_MatchingUser_ReturnsTrue() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        boolean result = securityService.isUserObject(testUserId.toString());

        assertTrue(result);
    }

    @Test
    @DisplayName("isUserObject - Should return false when user doesn't match")
    void isUserObject_DifferentUser_ReturnsFalse() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UUID differentUserId = UUID.randomUUID();

        boolean result = securityService.isUserObject(differentUserId.toString());

        assertFalse(result);
    }

    @Test
    @DisplayName("isUserObject - Should return false when authentication is null")
    void isUserObject_NoAuthentication_ReturnsFalse() {
        SecurityContextHolder.clearContext();

        boolean result = securityService.isUserObject(testUserId.toString());

        assertFalse(result);
    }

    @Test
    @DisplayName("isUserObject - Should return false when principal is not User")
    void isUserObject_NonUserPrincipal_ReturnsFalse() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("stringPrincipal", null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        boolean result = securityService.isUserObject(testUserId.toString());

        assertFalse(result);
    }

    @Test
    @DisplayName("isUserObject - Should handle invalid UUID format")
    void isUserObject_InvalidUUID_ThrowsException() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(IllegalArgumentException.class, () -> {
            securityService.isUserObject("invalid-uuid");
        });
    }

    @Test
    @DisplayName("isUserObject - Should return true for same user across multiple calls")
    void isUserObject_MultipleCalls_ConsistentResults() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        boolean result1 = securityService.isUserObject(testUserId.toString());
        boolean result2 = securityService.isUserObject(testUserId.toString());
        boolean result3 = securityService.isUserObject(testUserId.toString());

        assertTrue(result1);
        assertTrue(result2);
        assertTrue(result3);
    }
}
