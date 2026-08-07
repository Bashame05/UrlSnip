package com.makar.UrlSnip.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record UserLoginDto(
        @NotBlank(message = "Username cannot be blank")
        String userName,
        @NotBlank(message = "Password cannot be blank")
        String userPassword
) {
}
