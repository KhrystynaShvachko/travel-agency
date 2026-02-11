package com.epam.finaltask.service;

import com.epam.finaltask.dto.*;
import com.epam.finaltask.exception.AlreadyInUseException;
import com.epam.finaltask.exception.NotEnoughBalanceException;
import com.epam.finaltask.exception.ResourceNotFoundException;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import com.epam.finaltask.repository.specification.VoucherSpecifications;
import com.epam.finaltask.service.impl.VoucherServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoucherService Tests")
class VoucherServiceImplTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private VoucherMapper voucherMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenStorageService<VoucherPaginatedResponse> voucherPageStorage;

    @Mock
    private TokenStorageService<UserDTO> userTokenStorageService;

    @InjectMocks
    private VoucherServiceImpl voucherService;

    private Voucher testVoucher;
    private VoucherDTO testVoucherDTO;
    private User testUser;
    private UUID testVoucherId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testVoucherId = UUID.randomUUID();
        testUserId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");
        testUser.setBalance(BigDecimal.valueOf(500));

        testVoucher = new Voucher();
        testVoucher.setId(testVoucherId);
        testVoucher.setTitle("Test Voucher");
        testVoucher.setDescription("Test Description");
        testVoucher.setPrice(BigDecimal.valueOf(200));
        testVoucher.setTourType(TourType.LEISURE);
        testVoucher.setTransferType(TransferType.PLANE);
        testVoucher.setHotelType(HotelType.FIVE_STARS);
        testVoucher.setStatus(VoucherStatus.CREATED);
        testVoucher.setArrivalDate(LocalDate.now().plusDays(10));
        testVoucher.setEvictionDate(LocalDate.now().plusDays(20));
        testVoucher.setIsHot(false);

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
        testVoucherDTO.setIsHot(false);
    }

    @Test
    @DisplayName("create - Should create voucher successfully")
    void create_Success() {
        when(voucherMapper.toVoucher(any(VoucherDTO.class))).thenReturn(testVoucher);
        when(voucherRepository.save(any(Voucher.class))).thenReturn(testVoucher);
        when(voucherMapper.toVoucherDTO(any(Voucher.class))).thenReturn(testVoucherDTO);

        VoucherDTO result = voucherService.create(testVoucherDTO);

        assertNotNull(result);
        assertEquals(testVoucherDTO.getTitle(), result.getTitle());
        verify(voucherPageStorage).clearAll();
        verify(voucherRepository).save(any(Voucher.class));
    }

    @Test
    @DisplayName("order - Should throw ResourceNotFoundException when voucher not found")
    void order_VoucherNotFound_ThrowsException() {
        when(voucherRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            voucherService.order(testVoucherId.toString(), testUserId.toString());
        });

        verify(voucherRepository).findById(testVoucherId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("order - Should throw ResourceNotFoundException when user not found")
    void order_UserNotFound_ThrowsException() {
        when(voucherRepository.findById(any(UUID.class))).thenReturn(Optional.of(testVoucher));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            voucherService.order(testVoucherId.toString(), testUserId.toString());
        });

        verify(voucherRepository).findById(testVoucherId);
        verify(userRepository).findById(testUserId);
        verify(voucherRepository, never()).save(any(Voucher.class));
    }

    @Test
    @DisplayName("order - Should throw AlreadyInUseException when voucher already has a user")
    void order_VoucherAlreadyOrdered_ThrowsException() {
        testVoucher.setUser(testUser);

        when(voucherRepository.findById(any(UUID.class))).thenReturn(Optional.of(testVoucher));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));

        assertThrows(AlreadyInUseException.class, () -> {
            voucherService.order(testVoucherId.toString(), testUserId.toString());
        });

        verify(voucherRepository).findById(testVoucherId);
        verify(voucherRepository, never()).save(any(Voucher.class));
    }

    @Test
    @DisplayName("order - Should throw NotEnoughBalanceException when user has insufficient balance")
    void order_InsufficientBalance_ThrowsException() {
        testUser.setBalance(BigDecimal.valueOf(100));
        testVoucher.setPrice(BigDecimal.valueOf(200));

        when(voucherRepository.findById(any(UUID.class))).thenReturn(Optional.of(testVoucher));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));

        assertThrows(NotEnoughBalanceException.class, () -> {
            voucherService.order(testVoucherId.toString(), testUserId.toString());
        });

        verify(voucherRepository).findById(testVoucherId);
        verify(userRepository).findById(testUserId);
        verify(voucherRepository, never()).save(any(Voucher.class));
    }

    @Test
    @DisplayName("update - Should throw ResourceNotFoundException when voucher not found")
    void update_VoucherNotFound_ThrowsException() {
        when(voucherRepository.existsById(any(UUID.class))).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            voucherService.update(testVoucherId.toString(), testVoucherDTO);
        });

        verify(voucherRepository).existsById(testVoucherId);
        verify(voucherRepository, never()).save(any(Voucher.class));
    }

    @Test
    @DisplayName("getById - Should retrieve voucher successfully")
    void getById_Success() {
        when(voucherRepository.existsById(any(UUID.class))).thenReturn(true);
        when(voucherRepository.getReferenceById(any(UUID.class))).thenReturn(testVoucher);
        when(voucherMapper.toVoucherDTO(any(Voucher.class))).thenReturn(testVoucherDTO);

        VoucherDTO result = voucherService.getById(testVoucherId.toString());

        assertNotNull(result);
        assertEquals(testVoucherDTO.getId(), result.getId());
        verify(voucherRepository).existsById(testVoucherId);
        verify(voucherRepository).getReferenceById(testVoucherId);
    }

    @Test
    @DisplayName("getById - Should throw ResourceNotFoundException when voucher not found")
    void getById_VoucherNotFound_ThrowsException() {
        when(voucherRepository.existsById(any(UUID.class))).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            voucherService.getById(testVoucherId.toString());
        });

        verify(voucherRepository).existsById(testVoucherId);
        verify(voucherRepository, never()).getReferenceById(any(UUID.class));
    }

    @Test
    @DisplayName("delete - Should throw ResourceNotFoundException when voucher not found")
    void delete_VoucherNotFound_ThrowsException() {
        when(voucherRepository.existsById(any(UUID.class))).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            voucherService.delete(testVoucherId.toString());
        });

        verify(voucherRepository).existsById(testVoucherId);
        verify(voucherRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    @DisplayName("changeStatus - Should throw ResourceNotFoundException when voucher not found")
    void changeStatus_VoucherNotFound_ThrowsException() {
        VoucherStatusRequest statusRequest = new VoucherStatusRequest();
        statusRequest.setVoucherStatus("REGISTERED");

        when(voucherRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            voucherService.changeStatus(testVoucherId.toString(), statusRequest);
        });

        verify(voucherRepository).findById(testVoucherId);
        verify(voucherRepository, never()).save(any(Voucher.class));
    }

    @Test
    @DisplayName("changeStatus - Should throw DataIntegrityViolationException for invalid status")
    void changeStatus_InvalidStatus_ThrowsException() {
        VoucherStatusRequest statusRequest = new VoucherStatusRequest();
        statusRequest.setVoucherStatus("INVALID_STATUS");

        when(voucherRepository.findById(any(UUID.class))).thenReturn(Optional.of(testVoucher));

        assertThrows(DataIntegrityViolationException.class, () -> {
            voucherService.changeStatus(testVoucherId.toString(), statusRequest);
        });

        verify(voucherRepository).findById(testVoucherId);
    }
}
