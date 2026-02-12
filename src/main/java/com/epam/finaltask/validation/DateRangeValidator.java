package com.epam.finaltask.validation;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.validation.annotation.ValidDateRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (!(value instanceof VoucherDTO dto)) {
            return true;
        }

        LocalDate arrivalDate = dto.getArrivalDate();
        LocalDate evictionDate = dto.getEvictionDate();
        LocalDate today = LocalDate.now();

        if (arrivalDate == null || evictionDate == null) {
            return true;
        }

        boolean isValid = true;

        if (arrivalDate.isBefore(today)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Arrival date cannot be in the past")
                    .addPropertyNode("arrivalDate")
                    .addConstraintViolation();
            isValid = false;
        }

        if (!evictionDate.isAfter(arrivalDate)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Eviction date must be after arrival date")
                    .addPropertyNode("evictionDate")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}
