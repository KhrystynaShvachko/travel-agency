package com.epam.finaltask.controller;

import com.epam.finaltask.contoller.UserController;
import com.epam.finaltask.dto.TopUpRequest;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private UserDTO testUserDTO;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");
        testUser.setBalance(BigDecimal.valueOf(100));

        testUserDTO = UserDTO.builder()
                .id(testUserId.toString())
                .username("testuser")
                .email("test@example.com")
                .balance(BigDecimal.valueOf(100))
                .build();
    }

    @Test
    @DisplayName("dashboard - Should return dashboard page")
    void dashboard_ReturnsDashboard() {
        String result = userController.dashboard(model);

        assertEquals("user/dashboard", result);
    }

    @Test
    @DisplayName("profile - Should retrieve and display user profile")
    void profile_Success() {
        when(userService.getUserById(any(UUID.class))).thenReturn(testUserDTO);

        String result = userController.profile(testUserId.toString(), model);

        assertEquals("fragments/user-profile :: user-info-fragment", result);
        verify(userService).getUserById(testUserId);
        verify(model).addAttribute("user", testUserDTO);
    }

    @Test
    @DisplayName("editProfile - Should return edit form with user data")
    void editProfile_Success() {
        when(userService.getUserById(any(UUID.class))).thenReturn(testUserDTO);

        String result = userController.editProfile(testUserId.toString(), model);

        assertEquals("fragments/user-profile :: profile-edit-fragment", result);
        verify(userService).getUserById(testUserId);
        verify(model).addAttribute("user", testUserDTO);
    }

    @Test
    @DisplayName("updateProfile - Should update profile successfully")
    void updateProfile_Success() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.updateUser(anyString(), any(UserDTO.class))).thenReturn(testUserDTO);

        String result = userController.updateProfile(testUserDTO, bindingResult, response, model);

        assertEquals("fragments/user-profile :: user-info-fragment", result);
        verify(userService).updateUser(testUserDTO.getUsername(), testUserDTO);
        verify(model).addAttribute("user", testUserDTO);
    }

    @Test
    @DisplayName("updateProfile - Should return error form when validation fails")
    void updateProfile_ValidationError() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(bindingResult.hasErrors()).thenReturn(true);

        String result = userController.updateProfile(testUserDTO, bindingResult, response, model);

        assertEquals("fragments/user-profile :: profile-edit-fragment", result);
        assertEquals(422, response.getStatus());
        verify(userService, never()).updateUser(anyString(), any(UserDTO.class));
    }

    @Test
    @DisplayName("balance - Should return balance form")
    void balance_Success() {
        when(userService.getUserById(any(UUID.class))).thenReturn(testUserDTO);

        String result = userController.balance(testUser, testUserId.toString(), model);

        assertEquals("fragments/user-profile :: profile-balance-fragment", result);
        verify(userService).getUserById(testUserId);
        verify(model).addAttribute(eq("topup"), any(TopUpRequest.class));
        verify(model).addAttribute("user", testUserDTO);
    }

    @Test
    @DisplayName("updateBalance - Should top up balance successfully")
    void updateBalance_Success() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        TopUpRequest topUpRequest = new TopUpRequest();
        topUpRequest.setAmount(BigDecimal.valueOf(50));

        UserDTO updatedUserDTO = UserDTO.builder()
                .id(testUserId.toString())
                .username("testuser")
                .balance(BigDecimal.valueOf(150))
                .build();

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.changeBalance(anyString(), any(BigDecimal.class))).thenReturn(updatedUserDTO);

        String result = userController.updateBalance(testUserId.toString(), topUpRequest,
                bindingResult, response, model);

        assertEquals("fragments/user-profile :: profile-balance-fragment", result);
        verify(userService).changeBalance(testUserId.toString(), topUpRequest.getAmount());
        verify(model).addAttribute("user", updatedUserDTO);
    }

    @Test
    @DisplayName("updateBalance - Should return error form when validation fails")
    void updateBalance_ValidationError() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        TopUpRequest topUpRequest = new TopUpRequest();

        when(bindingResult.hasErrors()).thenReturn(true);
        when(userService.getUserById(any(UUID.class))).thenReturn(testUserDTO);

        String result = userController.updateBalance(testUserId.toString(), topUpRequest,
                bindingResult, response, model);

        assertEquals("fragments/user-profile :: profile-balance-fragment", result);
        assertEquals(422, response.getStatus());
        verify(userService, never()).changeBalance(anyString(), any(BigDecimal.class));
        verify(userService).getUserById(testUserId);
        verify(model).addAttribute("user", testUserDTO);
    }
}
