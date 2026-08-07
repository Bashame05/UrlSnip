package com.makar.UrlSnip.dto.auth;

import java.util.UUID;

public record UserResponseDto(
        UUID userId,
        String userName,
        String userEmail,
        String userRole,
        String createdAt
) {
}
