package com.makar.UrlSnip.mapper;

import com.makar.UrlSnip.dto.UrlResponseDto;
import com.makar.UrlSnip.model.UrlMapping;
import com.makar.UrlSnip.utils.ShortUrlBuilder;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UrlResponseMapper implements Function<UrlMapping, UrlResponseDto> {
   private final ShortUrlBuilder shortUrlBuilder;

    public UrlResponseMapper(ShortUrlBuilder shortUrlBuilder) {
        this.shortUrlBuilder = shortUrlBuilder;
    }

    @Override
    public UrlResponseDto apply(UrlMapping urlMapping) {
        return new UrlResponseDto(
                urlMapping.getLongUrl(),
                shortUrlBuilder.buildCompleteShortUrl(urlMapping),
                urlMapping.getCreatedAt()
        );
    }
}