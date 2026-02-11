package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRestController Tests")
class UserRestControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserRestController userRestController;

    private User testUser;
    private UserDTO testUserDTO;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");

        testUserDTO = UserDTO.builder()
                .id(testUserId.toString())
                .username("testuser")
                .email("test@example.com")
                .balance(BigDecimal.valueOf(100))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("getUserById - Should return user when accessing own profile")
    void getUserById_OwnProfile_Success() {
        when(userService.getUserById(any(UUID.class))).thenReturn(testUserDTO);

        ResponseEntity<UserDTO> result = userRestController.getUserById(testUser, testUserId.toString());

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testUserDTO.getId(), result.getBody().getId());
        verify(userService).getUserById(testUserId);
    }

    @Test
    @DisplayName("getUserById - Should throw AccessDeniedException when accessing another user's profile")
    void getUserById_OtherProfile_ThrowsException() {
        UUID otherUserId = UUID.randomUUID();

        assertThrows(AccessDeniedException.class, () -> {
            userRestController.getUserById(testUser, otherUserId.toString());
        });

        verify(userService, never()).getUserById(any(UUID.class));
    }

    @Test
    @DisplayName("updateUserById - Should update user when accessing own profile")
    void updateUserById_OwnProfile_Success() {
        when(userService.updateUser(anyString(), any(UserDTO.class))).thenReturn(testUserDTO);

        ResponseEntity<UserDTO> result = userRestController.updateUserById(testUser, "testuser", testUserDTO);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testUserDTO.getId(), result.getBody().getId());
        verify(userService).updateUser("testuser", testUserDTO);
    }

    @Test
    @DisplayName("updateUserById - Should throw AccessDeniedException when updating another user")
    void updateUserById_OtherProfile_ThrowsException() {
        UUID otherUserId = UUID.randomUUID();
        UserDTO otherUserDTO = UserDTO.builder()
                .id(otherUserId.toString())
                .username("otheruser")
                .build();

        assertThrows(AccessDeniedException.class, () -> {
            userRestController.updateUserById(testUser, "otheruser", otherUserDTO);
        });

        verify(userService, never()).updateUser(anyString(), any(UserDTO.class));
    }

    @Test
    @DisplayName("changeAccountStatus - Should toggle user account status")
    void changeAccountStatus_Success() {
        UserDTO inactiveUser = UserDTO.builder()
                .id(testUserId.toString())
                .username("testuser")
                .active(false)
                .build();

        when(userService.changeAccountStatus(any(UserDTO.class))).thenReturn(inactiveUser);

        ResponseEntity<UserDTO> result = userRestController.changeAccountStatus(testUserDTO);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().isActive());
        verify(userService).changeAccountStatus(testUserDTO);
    }

    @Test
    @DisplayName("changeAccountStatus - Should handle toggling from inactive to active")
    void changeAccountStatus_ToActive_Success() {
        testUserDTO.setActive(false);

        UserDTO activeUser = UserDTO.builder()
                .id(testUserId.toString())
                .username("testuser")
                .active(true)
                .build();

        when(userService.changeAccountStatus(any(UserDTO.class))).thenReturn(activeUser);

        ResponseEntity<UserDTO> result = userRestController.changeAccountStatus(testUserDTO);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isActive());
        verify(userService).changeAccountStatus(testUserDTO);
    }
}
