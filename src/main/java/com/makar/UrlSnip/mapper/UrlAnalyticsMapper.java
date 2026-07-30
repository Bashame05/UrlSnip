package com.makar.UrlSnip.mapper;

import com.makar.UrlSnip.dto.UrlAnalyticsDto;
import com.makar.UrlSnip.model.UrlMapping;
import com.makar.UrlSnip.utils.ShortUrlBuilder;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UrlAnalyticsMapper implements Function<UrlMapping, UrlAnalyticsDto> {
    private final ShortUrlBuilder shortUrlBuilder;

    public UrlAnalyticsMapper(ShortUrlBuilder shortUrlBuilder) {
        this.shortUrlBuilder = shortUrlBuilder;
    }

    @Override
    public UrlAnalyticsDto apply(UrlMapping urlMapping) {
        return new UrlAnalyticsDto(
                urlMapping.getLongUrl(),
                shortUrlBuilder.buildCompleteShortUrl(urlMapping),
                urlMapping.getCreatedAt(),
                urlMapping.getLastAccessed(),
                urlMapping.getClickCount()
        );
    }
}
