package com.makar.UrlSnip.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UrlRequestDto(
        String longUrl,
        String customAlias,
        @Min(value = 0 , message = "Expiration duration cannot be negative")
        @Max(value = 183 , message = "Expiration cannot exceed six months")
        Integer expiresInDays
) {
}
