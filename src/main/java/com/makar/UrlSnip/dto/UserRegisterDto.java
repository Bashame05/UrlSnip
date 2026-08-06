package com.makar.UrlSnip.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterDto(
        @NotBlank(message = "Username cannot be blank")
        @Size(min = 3, max = 20 , message = "Username should be between the length of 3 and 20")
        String userName,
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid Email format")
        String userEmail,
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 7 , message = "Password too short")
        String userPassword
) {
}
