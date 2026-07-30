package com.makar.UrlSnip.mapper;

import com.makar.UrlSnip.dto.UrlAnalyticsDto;
import com.makar.UrlSnip.model.UrlMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UrlAnalyticsMapper implements Function<UrlMapping, UrlAnalyticsDto> {
    private final String absoluteUrl;

    public UrlAnalyticsMapper(@Value("${ABSOLUTE_URL}") String absoluteUrl) {
        this.absoluteUrl = absoluteUrl;
    }

    @Override
    public UrlAnalyticsDto apply(UrlMapping urlMapping) {
        return new UrlAnalyticsDto(
                urlMapping.getLongUrl(),
                absoluteUrl.concat(urlMapping.getShortUrl()),
                urlMapping.getCreatedAt(),
                urlMapping.getLastAccessed(),
                urlMapping.getClickCount()
        );
    }
}
