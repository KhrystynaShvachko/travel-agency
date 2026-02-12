package com.epam.finaltask.dto;

import com.epam.finaltask.validation.annotation.UniqueEmail;
import com.epam.finaltask.validation.annotation.UniqueUsername;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

	private String id;

	@NotBlank(message = "{validation.auth.username.required}")
	@Size(min = 2, max = 16, message = "{validation.user.username.size}")
	@UniqueUsername(message = "{validation.username.exists}")
	private String username;

	@Pattern(regexp = "^[a-zA-Zа-яА-Я]{2,16}$|^$",
			message = "{validation.user.name.format}")
	private String firstName;

	@Pattern(regexp = "^[a-zA-Zа-яА-Я]{2,16}$|^$",
			message = "{validation.user.name.format}")
	private String lastName;

	private String role;

	@Pattern(regexp = "^\\+[0-9]{7,15}$|^$",
			message = "{validation.user.phone.format}")
	private String phoneNumber;

	@Pattern(regexp = "^$|^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,10}$",
			message = "{validation.user.email.format}")
	@UniqueEmail(message = "{validation.email.exists}")
	private String email;

	@Builder.Default
	@NotNull(message = "{validation.payment.amount.required}")
	@PositiveOrZero(message = "{validation.payment.amount.positive}")
	private BigDecimal balance = BigDecimal.ZERO;

	@Builder.Default
	private boolean active = true;
}
