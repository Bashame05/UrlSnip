package com.makar.urlshortner.dto;

import java.time.LocalDateTime;

public record UrlAnalyticsDto(
        String longUrl,
        String shortUrl,
        LocalDateTime createdAt,
        LocalDateTime lastAccessed,
        Long clickCount
) {
}
