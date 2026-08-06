package com.makar.UrlSnip.dto;

import java.util.UUID;

public record UserResponseDto(
        UUID userId,
        String userName,
        String userEmail,
        String userRole,
        String createdAt
) {
}
