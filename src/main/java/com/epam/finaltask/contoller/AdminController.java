package com.epam.finaltask.contoller;

import com.epam.finaltask.dto.AdminVoucherFilterRequest;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final VoucherService voucherService;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @GetMapping("/dashboard")
    public String admin() {
        return "admin/admin-page";
    }

    @GetMapping("/vouchers")
    public String getVouchersAdmin(Model model,
                                   AdminVoucherFilterRequest filterRequest,
                                   @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {

        model.addAttribute("vouchers", voucherService.findWithFilers(filterRequest, pageable));
        return "fragments/voucher-admin-list :: voucher-list-fragment";
    }

    @ResponseBody
    @DeleteMapping("/vouchers/{id}")
    public void deleteVoucher(@PathVariable UUID id) {
        voucherService.delete(id.toString());
    }

    @GetMapping("/vouchers/create")
    public String createForm(Model model) {
        model.addAttribute("voucher", new VoucherDTO());
        return "fragments/voucher-admin-list :: create-fragment";
    }

    @GetMapping("/row/{id}")
    public String getRow(@PathVariable UUID id, Model model) {
        VoucherDTO voucher = voucherService.getById(id.toString());
        model.addAttribute("voucher", voucher);
        return "fragments/voucher-admin-list :: voucher-row-view";
    }

    @PostMapping("/vouchers/create")
    public String processCreateVoucher(@ModelAttribute("voucher") @Valid VoucherDTO voucherDTO,
                                       BindingResult bindingResult,
                                       HttpServletResponse response,
                                       @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable,
                                       Model model) {

        if (bindingResult.hasErrors()) {
            response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
            return "fragments/voucher-admin-list :: create-fragment";
        }
        voucherService.create(voucherDTO);
        model.addAttribute("vouchers", voucherService.findWithFilers(new AdminVoucherFilterRequest(), pageable));
        return "fragments/voucher-admin-list :: voucher-list-fragment";
    }

    @GetMapping("/vouchers/edit/{id}")
    public String editFullRow(@PathVariable UUID id, Model model) {
        VoucherDTO voucher = voucherService.getById(id.toString());
        model.addAttribute("voucher", voucher);
        return "fragments/voucher-admin-list :: voucher-edit";
    }

    @PostMapping("/vouchers/update/{id}")
    public String updateVoucher(@PathVariable UUID id,
                                @ModelAttribute("voucher") @Valid VoucherDTO voucherDTO,
                                BindingResult bindingResult,
                                HttpServletResponse response,
                                @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable,
                                Model model) {

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while updating voucher {}: {}", id, bindingResult.getAllErrors());
            voucherDTO.setId(id.toString());

            response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
            return "fragments/voucher-admin-list :: voucher-edit";
        }

        voucherService.update(id.toString(), voucherDTO);

        model.addAttribute("vouchers", voucherService.findWithFilers(new AdminVoucherFilterRequest(), pageable));
        return "fragments/voucher-admin-list :: voucher-list-fragment";
    }
}