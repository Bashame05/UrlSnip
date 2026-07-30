package com.makar.UrlSnip.mapper;

import com.makar.UrlSnip.dto.UrlResponseDto;
import com.makar.UrlSnip.model.UrlMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UrlResponseMapper implements Function<UrlMapping, UrlResponseDto> {
    private final String absoluteUrl;

    public UrlResponseMapper(@Value("${ABSOLUTE_URL}") String absoluteUrl) {
        this.absoluteUrl = absoluteUrl;
    }

    @Override
    public UrlResponseDto apply(UrlMapping urlMapping) {
        return new UrlResponseDto(
                urlMapping.getLongUrl(),
                absoluteUrl.concat(urlMapping.getShortUrl()),
                urlMapping.getCreatedAt()
        );
    }
}