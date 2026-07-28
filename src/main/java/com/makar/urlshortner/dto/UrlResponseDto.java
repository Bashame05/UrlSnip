package com.makar.urlshortner.dto;

import java.time.LocalDateTime;

public record UrlResponseDto(
        String longUrl,
        String shortUrl,
        LocalDateTime createdAt
) {
}
