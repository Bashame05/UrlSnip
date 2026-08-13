package com.makar.UrlSnip.mapper;

import com.makar.UrlSnip.dto.url.UserFavouriteUrlDto;
import com.makar.UrlSnip.model.UrlMapping;
import com.makar.UrlSnip.utils.ShortUrlBuilder;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UserFavouriteUrlMapper implements Function<UrlMapping, UserFavouriteUrlDto> {
    private final ShortUrlBuilder shortUrlBuilder;
    public UserFavouriteUrlMapper(ShortUrlBuilder shortUrlBuilder) {
        this.shortUrlBuilder = shortUrlBuilder;
    }
    @Override
    public UserFavouriteUrlDto apply(UrlMapping urlMapping) {
        return new UserFavouriteUrlDto(
                urlMapping.getLongUrl(),
                shortUrlBuilder.buildCompleteShortUrl(urlMapping)
        );
    }
}
