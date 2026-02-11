package com.epam.finaltask.service;

import com.epam.finaltask.service.impl.AttemptServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttemptService Tests")
class AttemptServiceImplTest {

    @Mock
    private TokenStorageService<Integer> failedAttemptStorage;

    @InjectMocks
    private AttemptServiceImpl attemptService;

    private String testIp;

    @BeforeEach
    void setUp() {
        testIp = "192.168.1.1";
    }

    @Test
    @DisplayName("track - Should track first failed attempt")
    void track_FirstAttempt() {
        when(failedAttemptStorage.get(anyString())).thenReturn(null);

        attemptService.track(testIp);

        verify(failedAttemptStorage).get("login:fail:" + testIp);
        verify(failedAttemptStorage).store("login:fail:" + testIp, 1);
    }

    @Test
    @DisplayName("isBlocked - Should return false when no attempts recorded")
    void isBlocked_NoAttempts_ReturnsFalse() {
        when(failedAttemptStorage.get(anyString())).thenReturn(null);

        boolean result = attemptService.isBlocked(testIp);

        assertFalse(result);
        verify(failedAttemptStorage).get("login:fail:" + testIp);
    }

    @Test
    @DisplayName("clearBlocked - Should clear blocked IP")
    void clearBlocked_Success() {
        attemptService.clearBlocked(testIp);

        verify(failedAttemptStorage).revoke(testIp);
    }

    @Test
    @DisplayName("clearBlocked - Should handle multiple IPs")
    void clearBlocked_MultipleIps() {
        String ip1 = "192.168.1.1";
        String ip2 = "192.168.1.2";

        attemptService.clearBlocked(ip1);
        attemptService.clearBlocked(ip2);

        verify(failedAttemptStorage).revoke(ip1);
        verify(failedAttemptStorage).revoke(ip2);
    }
}
