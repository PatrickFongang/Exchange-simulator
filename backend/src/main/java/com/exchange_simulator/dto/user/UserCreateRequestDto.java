package com.exchange_simulator.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

public class UserCreateRequestDto {
    @Getter
    @NotBlank(message = "username field can't be empty")
    String username;

    @Getter
    @NotBlank(message = "email field can't be empty")
    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "email address is incorrect"
    )
    String email;

    @Getter
    String password;

    @Getter
    String role;
}
