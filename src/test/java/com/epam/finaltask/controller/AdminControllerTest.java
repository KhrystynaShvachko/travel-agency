package com.epam.finaltask.controller;

import com.epam.finaltask.contoller.AdminController;
import com.epam.finaltask.dto.AdminVoucherFilterRequest;
import com.epam.finaltask.dto.VoucherDTO;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController Tests")
class AdminControllerTest {

    @Mock
    private VoucherService voucherService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private AdminController adminController;

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
        testVoucherDTO.setTourType("VACATION");
        testVoucherDTO.setTransferType("PLANE");
        testVoucherDTO.setHotelType("FIVE_STARS");
        testVoucherDTO.setStatus("CREATED");
        testVoucherDTO.setArrivalDate(LocalDate.now().plusDays(10));
        testVoucherDTO.setEvictionDate(LocalDate.now().plusDays(20));

        paginatedResponse = new VoucherPaginatedResponse();
    }

    @Test
    @DisplayName("admin - Should return admin dashboard page")
    void admin_ReturnsDashboard() {
        String result = adminController.admin(model);

        assertEquals("admin/admin-page", result);
    }

    @Test
    @DisplayName("getVouchersAdmin - Should retrieve and display vouchers")
    void getVouchersAdmin_Success() {
        AdminVoucherFilterRequest filterRequest = new AdminVoucherFilterRequest();
        Pageable pageable = PageRequest.of(0, 10);

        when(voucherService.findWithFilers(any(AdminVoucherFilterRequest.class), any(Pageable.class)))
                .thenReturn(paginatedResponse);

        String result = adminController.getVouchersAdmin(model, filterRequest, pageable);

        assertEquals("fragments/voucher-admin-list :: voucher-list-fragment", result);
        verify(voucherService).findWithFilers(filterRequest, pageable);
        verify(model).addAttribute("vouchers", paginatedResponse);
    }

    @Test
    @DisplayName("deleteVoucher - Should delete voucher by ID")
    void deleteVoucher_Success() {
        assertDoesNotThrow(() -> {
            adminController.deleteVoucher(testVoucherId);
        });

        verify(voucherService).delete(testVoucherId.toString());
    }

    @Test
    @DisplayName("createForm - Should return create form with empty voucher")
    void createForm_Success() {
        String result = adminController.createForm(model);

        assertEquals("fragments/voucher-admin-list :: create-fragment", result);
        verify(model).addAttribute(eq("voucher"), any(VoucherDTO.class));
    }

    @Test
    @DisplayName("getRow - Should retrieve and display voucher row")
    void getRow_Success() {
        when(voucherService.getById(anyString())).thenReturn(testVoucherDTO);

        String result = adminController.getRow(testVoucherId, model);

        assertEquals("fragments/voucher-admin-list :: voucher-row-view", result);
        verify(voucherService).getById(testVoucherId.toString());
        verify(model).addAttribute("voucher", testVoucherDTO);
    }

    @Test
    @DisplayName("processCreateVoucher - Should create voucher successfully")
    void processCreateVoucher_Success() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        Pageable pageable = PageRequest.of(0, 10);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(voucherService.create(any(VoucherDTO.class))).thenReturn(testVoucherDTO);
        when(voucherService.findWithFilers(any(AdminVoucherFilterRequest.class), any(Pageable.class)))
                .thenReturn(paginatedResponse);

        String result = adminController.processCreateVoucher(testVoucherDTO, bindingResult, response, pageable, model);

        assertEquals("fragments/voucher-admin-list :: voucher-list-fragment", result);
        verify(voucherService).create(testVoucherDTO);
        verify(voucherService).findWithFilers(any(AdminVoucherFilterRequest.class), eq(pageable));
        verify(model).addAttribute("vouchers", paginatedResponse);
    }

    @Test
    @DisplayName("processCreateVoucher - Should return error form when validation fails")
    void processCreateVoucher_ValidationError() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        Pageable pageable = PageRequest.of(0, 10);

        when(bindingResult.hasErrors()).thenReturn(true);

        String result = adminController.processCreateVoucher(testVoucherDTO, bindingResult, response, pageable, model);

        assertEquals("fragments/voucher-admin-list :: create-fragment", result);
        assertEquals(422, response.getStatus());
        verify(voucherService, never()).create(any(VoucherDTO.class));
    }

    @Test
    @DisplayName("editFullRow - Should return edit form for voucher")
    void editFullRow_Success() {
        when(voucherService.getById(anyString())).thenReturn(testVoucherDTO);

        String result = adminController.editFullRow(testVoucherId, model);

        assertEquals("fragments/voucher-admin-list :: voucher-edit", result);
        verify(voucherService).getById(testVoucherId.toString());
        verify(model).addAttribute("voucher", testVoucherDTO);
    }

    @Test
    @DisplayName("updateVoucher - Should update voucher successfully")
    void updateVoucher_Success() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        Pageable pageable = PageRequest.of(0, 10);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(voucherService.update(anyString(), any(VoucherDTO.class))).thenReturn(testVoucherDTO);
        when(voucherService.findWithFilers(any(), any(Pageable.class)))
                .thenReturn(paginatedResponse);

        String result = adminController.updateVoucher(testVoucherId.toString(), testVoucherDTO,
                bindingResult, response, pageable, model);

        assertEquals("fragments/voucher-admin-list :: voucher-list-fragment", result);
        verify(voucherService).update(testVoucherId.toString(), testVoucherDTO);
        verify(model).addAttribute("vouchers", paginatedResponse);
    }

    @Test
    @DisplayName("updateVoucher - Should return error form when validation fails")
    void updateVoucher_ValidationError() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        Pageable pageable = PageRequest.of(0, 10);

        when(bindingResult.hasErrors()).thenReturn(true);

        String result = adminController.updateVoucher(testVoucherId.toString(), testVoucherDTO,
                bindingResult, response, pageable, model);

        assertEquals("fragments/voucher-admin-list :: voucher-edit", result);
        assertEquals(422, response.getStatus());
        assertEquals(testVoucherId.toString(), testVoucherDTO.getId());
        verify(voucherService, never()).update(anyString(), any(VoucherDTO.class));
    }
}
