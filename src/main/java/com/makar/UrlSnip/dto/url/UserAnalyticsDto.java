package com.makar.UrlSnip.dto.url;

public record UserAnalyticsDto(
        Long urlsCreated,
        Long totalRedirects
) {
    public UserAnalyticsDto {
        if (totalRedirects == null) {
            totalRedirects = 0L;
        }
    }
}
