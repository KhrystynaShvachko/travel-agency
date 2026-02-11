package com.epam.finaltask.restcontroller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoucherRestController Tests")
class VoucherRestControllerTest {

    @Mock
    private VoucherService voucherService;

    @InjectMocks
    private VoucherRestController voucherRestController;

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
    @DisplayName("getFilteredVouchers - Should return paginated vouchers")
    void getFilteredVouchers_Success() {
        VoucherFilerRequest filterRequest = new VoucherFilerRequest();
        Pageable pageable = PageRequest.of(0, 10);

        when(voucherService.findWithFilers(any(VoucherFilerRequest.class), any(Pageable.class)))
                .thenReturn(paginatedResponse);

        ResponseEntity<PaginatedResponse<VoucherDTO>> result =
                voucherRestController.getFilteredVouchers(filterRequest, pageable);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(voucherService).findWithFilers(filterRequest, pageable);
    }

    @Test
    @DisplayName("getAdminFilteredVouchers - Should return paginated vouchers for admin")
    void getAdminFilteredVouchers_Success() {
        PersonalVoucherFilterRequest filterRequest = new PersonalVoucherFilterRequest();
        Pageable pageable = PageRequest.of(0, 20);

        when(voucherService.findWithFilers(any(PersonalVoucherFilterRequest.class), any(Pageable.class)))
                .thenReturn(paginatedResponse);

        ResponseEntity<PaginatedResponse<VoucherDTO>> result =
                voucherRestController.getAdminFilteredVouchers(filterRequest, pageable);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(voucherService).findWithFilers(filterRequest, pageable);
    }

    @Test
    @DisplayName("adminFindAllByUserId - Should return OK response")
    void adminFindAllByUserId_Success() {
        Pageable pageable = PageRequest.of(0, 20);

        ResponseEntity<PaginatedResponse<VoucherDTO>> result =
                voucherRestController.adminFindAllByUserId(testUserId.toString(), pageable);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    @DisplayName("createVoucher - Should create voucher and return CREATED status")
    void createVoucher_Success() {
        when(voucherService.create(any(VoucherDTO.class))).thenReturn(testVoucherDTO);

        ResponseEntity<Void> result = voucherRestController.createVoucher(testVoucherDTO);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        verify(voucherService).create(testVoucherDTO);
    }

    @Test
    @DisplayName("updateVoucher - Should update voucher and return OK status")
    void updateVoucher_Success() {
        when(voucherService.update(anyString(), any(VoucherDTO.class))).thenReturn(testVoucherDTO);

        ResponseEntity<Void> result = voucherRestController.updateVoucher(testVoucherId.toString(), testVoucherDTO);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(voucherService).update(testVoucherId.toString(), testVoucherDTO);
    }

    @Test
    @DisplayName("deleteVoucherById - Should delete voucher and return OK status")
    void deleteVoucherById_Success() {
        ResponseEntity<Void> result = voucherRestController.deleteVoucherById(testVoucherId.toString());

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(voucherService).delete(testVoucherId.toString());
    }

    @Test
    @DisplayName("changeVoucherStatus - Should change status and return OK status")
    void changeVoucherStatus_Success() {
        VoucherStatusRequest statusRequest = new VoucherStatusRequest();
        statusRequest.setVoucherStatus("REGISTERED");

        when(voucherService.changeStatus(anyString(), any(VoucherStatusRequest.class)))
                .thenReturn(testVoucherDTO);

        ResponseEntity<Void> result = voucherRestController.changeVoucherStatus(testVoucherId.toString(), statusRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(voucherService).changeStatus(testVoucherId.toString(), statusRequest);
    }

    @Test
    @DisplayName("orderVoucher - Should order voucher and return OK status")
    void orderVoucher_Success() {
        when(voucherService.order(anyString(), anyString())).thenReturn(testVoucherDTO);

        ResponseEntity<VoucherDTO> result = voucherRestController.orderVoucher(testUser, testVoucherId.toString());

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(voucherService).order(testVoucherId.toString(), testUserId.toString());
    }

    @Test
    @DisplayName("orderVoucher - Should use authenticated user's ID")
    void orderVoucher_UsesAuthenticatedUserId() {
        UUID authenticatedUserId = UUID.randomUUID();
        User authenticatedUser = new User();
        authenticatedUser.setId(authenticatedUserId);

        when(voucherService.order(anyString(), anyString())).thenReturn(testVoucherDTO);

        voucherRestController.orderVoucher(authenticatedUser, testVoucherId.toString());

        verify(voucherService).order(testVoucherId.toString(), authenticatedUserId.toString());
    }
}
