package com.epam.finaltask.service;

import com.epam.finaltask.dto.PaginatedResponse;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.AlreadyInUseException;
import com.epam.finaltask.exception.ResourceNotFoundException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TokenStorageService<UserDTO> userTokenStorageService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDTO testUserDTO;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setBalance(BigDecimal.valueOf(100));
        testUser.setActive(true);
        testUser.setPassword("encodedPassword");

        testUserDTO = UserDTO.builder()
                .id(testUserId.toString())
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .balance(BigDecimal.valueOf(100))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("saveUser - Should save user successfully")
    void saveUser_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toUser(any(UserDTO.class))).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toUserDTO(any(User.class))).thenReturn(testUserDTO);

        UserDTO result = userService.saveUser(testUserDTO, "password123");

        assertNotNull(result);
        assertEquals(testUserDTO.getUsername(), result.getUsername());
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("saveUser - Should throw AlreadyInUseException when username exists")
    void saveUser_UsernameExists_ThrowsException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(AlreadyInUseException.class, () -> {
            userService.saveUser(testUserDTO, "password123");
        });

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("saveUser - Should throw AlreadyInUseException when email exists")
    void saveUser_EmailExists_ThrowsException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(AlreadyInUseException.class, () -> {
            userService.saveUser(testUserDTO, "password123");
        });

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser - Should update user successfully")
    void updateUser_Success() {
        when(userRepository.findUserByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot(anyString(), any(UUID.class))).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toUserDTO(any(User.class))).thenReturn(testUserDTO);
        doNothing().when(userMapper).updateEntityFromDto(any(UserDTO.class), any(User.class));

        UserDTO updatedDTO = UserDTO.builder()
                .username("testuser")
                .email("newemail@example.com")
                .firstName("Updated")
                .lastName("User")
                .build();

        UserDTO result = userService.updateUser("testuser", updatedDTO);

        assertNotNull(result);
        verify(userRepository).findUserByUsername("testuser");
        verify(userRepository).save(any(User.class));
        verify(userTokenStorageService, times(2)).revoke(anyString());
    }

    @Test
    @DisplayName("updateUser - Should throw ResourceNotFoundException when user not found")
    void updateUser_UserNotFound_ThrowsException() {
        when(userRepository.findUserByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser("nonexistent", testUserDTO);
        });

        verify(userRepository).findUserByUsername("nonexistent");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser - Should throw AlreadyInUseException when email already in use")
    void updateUser_EmailAlreadyInUse_ThrowsException() {
        when(userRepository.findUserByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot(anyString(), any(UUID.class))).thenReturn(true);

        UserDTO updatedDTO = UserDTO.builder()
                .email("existing@example.com")
                .build();

        assertThrows(AlreadyInUseException.class, () -> {
            userService.updateUser("testuser", updatedDTO);
        });

        verify(userRepository).existsByEmailAndIdNot("existing@example.com", testUserId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("changeBalance - Should increase balance successfully")
    void changeBalance_Success() {
        BigDecimal addAmount = BigDecimal.valueOf(50);
        BigDecimal expectedBalance = BigDecimal.valueOf(150);

        User updatedUser = new User();
        updatedUser.setId(testUserId);
        updatedUser.setUsername("testuser");
        updatedUser.setBalance(expectedBalance);

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toUserDTO(any(User.class))).thenReturn(testUserDTO);

        UserDTO result = userService.changeBalance(testUserId.toString(), addAmount);

        assertNotNull(result);
        verify(userRepository).findById(testUserId);
        verify(userRepository).save(any(User.class));
        verify(userTokenStorageService, times(2)).revoke(anyString());
    }

    @Test
    @DisplayName("changeBalance - Should throw exception when amount is negative")
    void changeBalance_NegativeAmount_ThrowsException() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));

        assertThrows(RuntimeException.class, () -> {
            userService.changeBalance(testUserId.toString(), BigDecimal.valueOf(-10));
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("changeBalance - Should throw exception when amount is null")
    void changeBalance_NullAmount_ThrowsException() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));

        assertThrows(RuntimeException.class, () -> {
            userService.changeBalance(testUserId.toString(), null);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("changeBalance - Should throw ResourceNotFoundException when user not found")
    void changeBalance_UserNotFound_ThrowsException() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.changeBalance(UUID.randomUUID().toString(), BigDecimal.TEN);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("getUserByUsername - Should return cached user")
    void getUserByUsername_FromCache() {
        when(userTokenStorageService.get(anyString())).thenReturn(testUserDTO);

        UserDTO result = userService.getUserByUsername("testuser");

        assertNotNull(result);
        assertEquals(testUserDTO.getUsername(), result.getUsername());
        verify(userTokenStorageService).get("testuser");
        verify(userRepository, never()).findUserByUsername(anyString());
    }

    @Test
    @DisplayName("getUserByUsername - Should fetch from database and cache")
    void getUserByUsername_FromDatabase() {
        when(userTokenStorageService.get(anyString())).thenReturn(null);
        when(userRepository.findUserByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(userMapper.toUserDTO(any(User.class))).thenReturn(testUserDTO);

        UserDTO result = userService.getUserByUsername("testuser");

        assertNotNull(result);
        assertEquals(testUserDTO.getUsername(), result.getUsername());
        verify(userRepository).findUserByUsername("testuser");
        verify(userTokenStorageService).store(testUserDTO.getId(), testUserDTO);
        verify(userTokenStorageService).store(testUserDTO.getUsername(), testUserDTO);
    }

    @Test
    @DisplayName("getUserByUsername - Should throw ResourceNotFoundException when user not found")
    void getUserByUsername_NotFound_ThrowsException() {
        when(userTokenStorageService.get(anyString())).thenReturn(null);
        when(userRepository.findUserByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserByUsername("nonexistent");
        });

        verify(userRepository).findUserByUsername("nonexistent");
    }

    @Test
    @DisplayName("getUserById - Should return cached user")
    void getUserById_FromCache() {
        when(userTokenStorageService.get(anyString())).thenReturn(testUserDTO);

        UserDTO result = userService.getUserById(testUserId);

        assertNotNull(result);
        assertEquals(testUserDTO.getId(), result.getId());
        verify(userTokenStorageService).get(testUserId.toString());
        verify(userRepository, never()).findById(any(UUID.class));
    }

    @Test
    @DisplayName("getUserById - Should fetch from database and cache")
    void getUserById_FromDatabase() {
        when(userTokenStorageService.get(anyString())).thenReturn(null);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(userMapper.toUserDTO(any(User.class))).thenReturn(testUserDTO);

        UserDTO result = userService.getUserById(testUserId);

        assertNotNull(result);
        assertEquals(testUserDTO.getId(), result.getId());
        verify(userRepository).findById(testUserId);
        verify(userTokenStorageService).store(testUserDTO.getId(), testUserDTO);
        verify(userTokenStorageService).store(testUserDTO.getUsername(), testUserDTO);
    }

    @Test
    @DisplayName("getUserById - Should throw ResourceNotFoundException when user not found")
    void getUserById_NotFound_ThrowsException() {
        UUID randomId = UUID.randomUUID();
        when(userTokenStorageService.get(anyString())).thenReturn(null);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(randomId);
        });

        verify(userRepository).findById(randomId);
    }

    @Test
    @DisplayName("getUserByEmail - Should return user successfully")
    void getUserByEmail_Success() {
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userMapper.toUserDTO(any(User.class))).thenReturn(testUserDTO);

        UserDTO result = userService.getUserByEmail("test@example.com");

        assertNotNull(result);
        assertEquals(testUserDTO.getEmail(), result.getEmail());
        verify(userRepository).findUserByEmail("test@example.com");
    }

    @Test
    @DisplayName("getUserByEmail - Should throw ResourceNotFoundException when user not found")
    void getUserByEmail_NotFound_ThrowsException() {
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserByEmail("nonexistent@example.com");
        });

        verify(userRepository).findUserByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("changePassword - Should change password successfully")
    void changePassword_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);
        when(userMapper.toUser(any(UserDTO.class))).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toUserDTO(any(User.class))).thenReturn(testUserDTO);

        assertDoesNotThrow(() -> {
            userService.changePassword(testUserDTO, "newPassword123");
        });

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("changePassword - Should throw ResourceNotFoundException when user not found")
    void changePassword_UserNotFound_ThrowsException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.changePassword(testUserDTO, "newPassword123");
        });

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    /*@Test
    @DisplayName("getAllUsers - Should return paginated users")
    void getAllUsers_Success() {
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toUserDTO(any(User.class))).thenReturn(testUserDTO);

        Pageable pageable = PageRequest.of(0, 10);
        PaginatedResponse<UserDTO> result = userService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(pageable);
    }*/

    @Test
    @DisplayName("userDetailsService - Should return UserDetailsService")
    void userDetailsService_ReturnsService() {
        when(userRepository.findUserByUsername(anyString())).thenReturn(Optional.of(testUser));

        var userDetailsService = userService.userDetailsService();

        assertNotNull(userDetailsService);
        assertDoesNotThrow(() -> {
            userDetailsService.loadUserByUsername("testuser");
        });
    }
}
