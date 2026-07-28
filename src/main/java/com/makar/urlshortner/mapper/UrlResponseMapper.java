package com.makar.urlshortner.mapper;

import com.makar.urlshortner.dto.UrlResponseDto;
import com.makar.urlshortner.model.UrlMapping;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UrlResponseMapper implements Function<UrlMapping, UrlResponseDto> {
    @Override
    public UrlResponseDto apply(UrlMapping urlMapping) {
        return new UrlResponseDto(
                urlMapping.getLongUrl(),
                urlMapping.getShortUrl(),
                urlMapping.getCreatedAt()
        );
    }
}
