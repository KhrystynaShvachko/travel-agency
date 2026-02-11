package com.epam.finaltask.controller;

import com.epam.finaltask.contoller.ManagerController;
import com.epam.finaltask.dto.AdminVoucherFilterRequest;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.VoucherPaginatedResponse;
import com.epam.finaltask.dto.VoucherStatusRequest;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManagerController Tests")
class ManagerControllerTest {

    @Mock
    private VoucherService voucherService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private ManagerController managerController;

    private VoucherDTO testVoucherDTO;
    private UUID testVoucherId;
    private VoucherPaginatedResponse paginatedResponse;

    @BeforeEach
    void setUp() {
        testVoucherId = UUID.randomUUID();

        testVoucherDTO = new VoucherDTO();
        testVoucherDTO.setId(testVoucherId.toString());
        testVoucherDTO.setTitle("Test Voucher");
        testVoucherDTO.setDescription("Test Description");
        testVoucherDTO.setPrice(BigDecimal.valueOf(200));
        testVoucherDTO.setStatus("CREATED");
        testVoucherDTO.setIsHot(false);

        paginatedResponse = new VoucherPaginatedResponse();
    }

    @Test
    @DisplayName("getManagerPage - Should return manager dashboard")
    void getManagerPage_ReturnsDashboard() {
        String result = managerController.getManagerPage(model);

        assertEquals("manager/manager-page", result);
    }

    @Test
    @DisplayName("getVouchersManager - Should retrieve and display vouchers")
    void getVouchersManager_Success() {
        AdminVoucherFilterRequest filterRequest = new AdminVoucherFilterRequest();
        Pageable pageable = PageRequest.of(0, 10);

        when(voucherService.findWithFilers(any(AdminVoucherFilterRequest.class), any(Pageable.class)))
                .thenReturn(paginatedResponse);

        String result = managerController.getVouchersManager(model, filterRequest, pageable);

        assertEquals("fragments/voucher-manager-list :: voucher-list-fragment", result);
        verify(voucherService).findWithFilers(filterRequest, pageable);
        verify(model).addAttribute("vouchers", paginatedResponse);
    }

    @Test
    @DisplayName("editRow - Should return edit form with voucher and status request")
    void editRow_Success() {
        when(voucherService.getById(anyString())).thenReturn(testVoucherDTO);

        String result = managerController.editRow(testVoucherId, model);

        assertEquals("fragments/voucher-manager-list :: voucher-row-edit", result);
        verify(voucherService).getById(testVoucherId.toString());
        verify(model).addAttribute(eq("voucher"), any(VoucherDTO.class));
        verify(model).addAttribute(eq("voucherStatusRequest"), any(VoucherStatusRequest.class));
    }

    @Test
    @DisplayName("getRow - Should retrieve and display voucher row")
    void getRow_Success() {
        when(voucherService.getById(anyString())).thenReturn(testVoucherDTO);

        String result = managerController.getRow(testVoucherId, model);

        assertEquals("fragments/voucher-manager-list :: voucher-row-view", result);
        verify(voucherService).getById(testVoucherId.toString());
        verify(model).addAttribute("voucher", testVoucherDTO);
    }

    @Test
    @DisplayName("updateVoucher - Should update voucher status successfully")
    void updateVoucher_Success() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AdminVoucherFilterRequest filterRequest = new AdminVoucherFilterRequest();
        Pageable pageable = PageRequest.of(0, 10);
        VoucherStatusRequest statusRequest = new VoucherStatusRequest();
        statusRequest.setVoucherStatus("REGISTERED");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(voucherService.changeStatus(anyString(), any(VoucherStatusRequest.class)))
                .thenReturn(testVoucherDTO);
        when(voucherService.findWithFilers(any(AdminVoucherFilterRequest.class), any(Pageable.class)))
                .thenReturn(paginatedResponse);

        String result = managerController.updateVoucher(testVoucherId.toString(), statusRequest,
                bindingResult, response, filterRequest, pageable, model);

        assertEquals("fragments/voucher-manager-list :: voucher-list-fragment", result);
        verify(voucherService).changeStatus(testVoucherId.toString(), statusRequest);
        verify(voucherService).findWithFilers(filterRequest, pageable);
        verify(model).addAttribute("vouchers", paginatedResponse);
    }

    @Test
    @DisplayName("updateVoucher - Should return error view when validation fails")
    void updateVoucher_ValidationError() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AdminVoucherFilterRequest filterRequest = new AdminVoucherFilterRequest();
        Pageable pageable = PageRequest.of(0, 10);
        VoucherStatusRequest statusRequest = new VoucherStatusRequest();

        when(bindingResult.hasErrors()).thenReturn(true);
        when(voucherService.getById(anyString())).thenReturn(testVoucherDTO);

        String result = managerController.updateVoucher(testVoucherId.toString(), statusRequest,
                bindingResult, response, filterRequest, pageable, model);

        assertEquals("fragments/voucher-manager-list :: voucher-row-view", result);
        assertEquals(422, response.getStatus());
        verify(voucherService, never()).changeStatus(anyString(), any(VoucherStatusRequest.class));
        verify(voucherService).getById(testVoucherId.toString());
        verify(model).addAttribute("voucher", testVoucherDTO);
    }
}
