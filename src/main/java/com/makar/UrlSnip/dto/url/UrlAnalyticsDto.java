package com.makar.UrlSnip.dto.url;

import java.time.LocalDateTime;

public record UrlAnalyticsDto(
        String longUrl,
        String shortUrl,
        LocalDateTime createdAt,
        LocalDateTime lastAccessed,
        LocalDateTime expiresAt,
        Long clickCount
) {
}
