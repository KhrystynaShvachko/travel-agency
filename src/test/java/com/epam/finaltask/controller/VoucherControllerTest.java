package com.epam.finaltask.controller;

import com.epam.finaltask.contoller.VoucherController;
import com.epam.finaltask.dto.*;
import com.epam.finaltask.model.User;
import com.epam.finaltask.model.VoucherPaginatedResponse;
import com.epam.finaltask.service.VoucherService;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoucherController Tests")
class VoucherControllerTest {

    @Mock
    private VoucherService voucherService;

    @Mock
    private Model model;

    @InjectMocks
    private VoucherController voucherController;

    private User testUser;
    private VoucherDTO testVoucherDTO;
    private UUID testVoucherId;
    private UUID testUserId;
    private VoucherPaginatedResponse paginatedResponse;

    @BeforeEach
    void setUp() {
        testVoucherId = UUID.randomUUID();
        testUserId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");

        testVoucherDTO = new VoucherDTO();
        testVoucherDTO.setId(testVoucherId.toString());
        testVoucherDTO.setTitle("Test Voucher");
        testVoucherDTO.setPrice(BigDecimal.valueOf(200));

        paginatedResponse = new VoucherPaginatedResponse();
    }

    @Test
    @DisplayName("getFilteredVouchers - Should retrieve and display filtered vouchers")
    void getFilteredVouchers_Success() {
        VoucherFilerRequest filterRequest = new VoucherFilerRequest();
        Pageable pageable = PageRequest.of(0, 10);

        when(voucherService.findWithFilers(any(VoucherFilerRequest.class), any(Pageable.class)))
                .thenReturn(paginatedResponse);

        String result = voucherController.getFilteredVouchers(model, filterRequest, pageable);

        assertEquals("fragments/voucher-list :: voucher-list-fragment", result);
        verify(voucherService).findWithFilers(filterRequest, pageable);
        verify(model).addAttribute("vouchers", paginatedResponse);
    }

    @Test
    @DisplayName("getFilteredVouchers - Should handle empty filters")
    void getFilteredVouchers_EmptyFilters() {
        VoucherFilerRequest filterRequest = new VoucherFilerRequest();
        Pageable pageable = PageRequest.of(0, 10);

        when(voucherService.findWithFilers(any(VoucherFilerRequest.class), any(Pageable.class)))
                .thenReturn(paginatedResponse);

        String result = voucherController.getFilteredVouchers(model, filterRequest, pageable);

        assertEquals("fragments/voucher-list :: voucher-list-fragment", result);
        verify(voucherService).findWithFilers(filterRequest, pageable);
    }

    @Test
    @DisplayName("orderVoucher - Should order voucher successfully")
    void orderVoucher_Success() {
        Pageable pageable = PageRequest.of(0, 10);

        when(voucherService.order(anyString(), anyString())).thenReturn(testVoucherDTO);
        when(voucherService.findWithFilers(any(VoucherFilerRequest.class), any(Pageable.class)))
                .thenReturn(paginatedResponse);

        String result = voucherController.orderVoucher(model, testUser,
                testVoucherId.toString(), pageable);

        assertEquals("fragments/voucher-list :: voucher-list-fragment", result);
        verify(voucherService).order(testVoucherId.toString(), testUserId.toString());
        verify(voucherService).findWithFilers(any(VoucherFilerRequest.class), eq(pageable));
        verify(model).addAttribute("vouchers", paginatedResponse);
        verify(model).addAttribute("message", "Successfully ordered voucher!");
    }

    @Test
    @DisplayName("getUserVouchers - Should retrieve user's vouchers")
    void getUserVouchers_Success() {
        PersonalVoucherFilterRequest filterRequest = new PersonalVoucherFilterRequest();
        Pageable pageable = PageRequest.of(0, 10);

        when(voucherService.findAllByUserId(any(PersonalVoucherFilterRequest.class), any(Pageable.class)))
                .thenReturn(paginatedResponse);

        String result = voucherController.getUserVouchers(testUserId.toString(),
                filterRequest, pageable, model);

        assertEquals("fragments/voucher-profile-list :: voucher-profile-list-fragment", result);
        assertEquals(testUserId, filterRequest.getUserId());
        verify(voucherService).findAllByUserId(filterRequest, pageable);
        verify(model).addAttribute("vouchers", paginatedResponse);
    }

    @Test
    @DisplayName("getUserVouchers - Should set userId in filter request")
    void getUserVouchers_SetsUserId() {
        PersonalVoucherFilterRequest filterRequest = new PersonalVoucherFilterRequest();
        Pageable pageable = PageRequest.of(0, 10);

        when(voucherService.findAllByUserId(any(PersonalVoucherFilterRequest.class), any(Pageable.class)))
                .thenReturn(paginatedResponse);

        voucherController.getUserVouchers(testUserId.toString(), filterRequest, pageable, model);

        assertNotNull(filterRequest.getUserId());
        assertEquals(testUserId.toString(), filterRequest.getUserId().toString());
    }
}
