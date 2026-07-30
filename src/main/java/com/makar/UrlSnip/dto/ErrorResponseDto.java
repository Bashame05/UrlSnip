package com.makar.UrlSnip.dto;


import java.time.Instant;

public record ErrorResponseDto(
        Instant timestamp,
        Integer status,
        String error,
        String message
) {
}
