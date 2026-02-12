package com.epam.finaltask.dto;

import com.epam.finaltask.validation.annotation.UniqueEmail;
import com.epam.finaltask.validation.annotation.UniqueUsername;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "{validation.auth.username.required}")
    @Size(min = 2, max = 16, message = "{validation.user.username.size}")
    @UniqueUsername(message = "{validation.username.exists}")
    private String username;

    @NotBlank(message = "{validation.auth.password.required}")
    @Size(min = 5, max = 15, message = "{validation.password.size}")
    @Pattern(regexp = "^\\S+$",
            message = "{validation.password.no_spaces}")
    @Pattern(regexp = ".*[0-9].*",
            message = "{validation.password.digit}")
    @Pattern(regexp = ".*[a-z].*",
            message = "{validation.password.lowercase}")
    @Pattern(regexp = ".*[A-Z].*",
            message = "{validation.password.uppercase}")
    @Pattern(regexp = ".*[!@#$%&*()+=^.-].*",
            message = "{validation.password.special}")
    @ToString.Exclude
    private String password;

    @Pattern(regexp = "^[a-zA-Zа-яА-Я]{2,16}$|^$",
            message = "{validation.user.name.format}")
    private String firstName;

    @Pattern(regexp = "^[a-zA-Zа-яА-Я]{2,16}$|^$",
            message = "{validation.user.name.format}")
    private String lastName;

    @Pattern(regexp = "^\\+[0-9]{7,15}$",
            message = "{validation.user.phone.format}")
    private String phoneNumber;

    @Pattern(regexp = "^$|^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,10}$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "{validation.user.email.format}")
    @UniqueEmail(message = "{validation.email.exists}")
    private String email;
}
