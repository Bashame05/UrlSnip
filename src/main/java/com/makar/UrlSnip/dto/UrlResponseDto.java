package com.makar.UrlSnip.dto;

import java.time.LocalDateTime;

public record UrlResponseDto(
        String longUrl,
        String shortUrl,
        LocalDateTime createdAt,
        LocalDateTime expiresIn
) {
}
