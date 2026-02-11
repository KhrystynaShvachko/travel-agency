package com.epam.finaltask.controller;

import com.epam.finaltask.contoller.AdminUserController;
import com.epam.finaltask.dto.PaginatedResponse;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserController Tests")
class AdminUserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @InjectMocks
    private AdminUserController adminUserController;

    private UserDTO testUserDTO;
    private PaginatedResponse<UserDTO> paginatedResponse;

    @BeforeEach
    void setUp() {
        testUserDTO = UserDTO.builder()
                .id("test-id")
                .username("testuser")
                .email("test@example.com")
                .active(true)
                .balance(BigDecimal.ZERO)
                .build();

        paginatedResponse = new PaginatedResponse<>();
    }

    @Test
    @DisplayName("getAllUsers - Should retrieve and display all users")
    void getAllUsers_Success() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(paginatedResponse);

        String result = adminUserController.getAllUsers(model, pageable);

        assertEquals("fragments/user-admin-list :: user-list-fragment", result);
        verify(userService).getAllUsers(pageable);
        verify(model).addAttribute("users", paginatedResponse);
    }

    @Test
    @DisplayName("getAllUsers - Should handle different page sizes")
    void getAllUsers_DifferentPageSize() {
        Pageable pageable = PageRequest.of(2, 25);

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(paginatedResponse);

        String result = adminUserController.getAllUsers(model, pageable);

        assertEquals("fragments/user-admin-list :: user-list-fragment", result);
        verify(userService).getAllUsers(pageable);
    }

    @Test
    @DisplayName("blockUser - Should toggle user status successfully")
    void blockUser_Success() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userService.changeAccountStatus(any(UserDTO.class))).thenReturn(testUserDTO);
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(paginatedResponse);

        String result = adminUserController.blockUser(testUserDTO, model, pageable);

        assertEquals("fragments/user-admin-list :: user-list-fragment", result);
        verify(userService).changeAccountStatus(testUserDTO);
        verify(userService).getAllUsers(pageable);
        verify(model).addAttribute("users", paginatedResponse);
    }

    @Test
    @DisplayName("blockUser - Should handle active user toggle")
    void blockUser_ActiveUser() {
        testUserDTO.setActive(true);
        Pageable pageable = PageRequest.of(0, 10);

        UserDTO inactiveUser = UserDTO.builder()
                .id("test-id")
                .username("testuser")
                .active(false)
                .build();

        when(userService.changeAccountStatus(any(UserDTO.class))).thenReturn(inactiveUser);
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(paginatedResponse);

        String result = adminUserController.blockUser(testUserDTO, model, pageable);

        assertEquals("fragments/user-admin-list :: user-list-fragment", result);
        verify(userService).changeAccountStatus(testUserDTO);
    }

    @Test
    @DisplayName("blockUser - Should handle inactive user toggle")
    void blockUser_InactiveUser() {
        testUserDTO.setActive(false);
        Pageable pageable = PageRequest.of(0, 10);

        UserDTO activeUser = UserDTO.builder()
                .id("test-id")
                .username("testuser")
                .active(true)
                .build();

        when(userService.changeAccountStatus(any(UserDTO.class))).thenReturn(activeUser);
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(paginatedResponse);

        String result = adminUserController.blockUser(testUserDTO, model, pageable);

        assertEquals("fragments/user-admin-list :: user-list-fragment", result);
        verify(userService).changeAccountStatus(testUserDTO);
    }
}
