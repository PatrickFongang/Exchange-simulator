package com.exchange_simulator.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class UserCreateRequestDto {
    @Getter
    @NotBlank(message = "username field can't be empty")
    String username;

    @Getter
    @NotBlank(message = "email field can't be empty")
    @Email(message = "email address is incorrect")
    String email;

    @Getter
    String password;

    @Getter
    String role;
}
