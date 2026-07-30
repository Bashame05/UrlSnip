package com.makar.UrlSnip.utils;

import com.makar.UrlSnip.model.UrlMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ShortUrlBuilder {
    private final String absoluteUrl;
    public ShortUrlBuilder(@Value("${ABSOLUTE_URL}") String absoluteUrl) {
        this.absoluteUrl = absoluteUrl;
    }
    public String buildCompleteShortUrl(UrlMapping urlMapping) {
        String shortUrl;
        if(urlMapping.getCustomAlias() != null) {
            shortUrl = absoluteUrl.concat(urlMapping.getCustomAlias());
        }else{
            shortUrl = absoluteUrl.concat(urlMapping.getShortUrl());
        }
        return shortUrl;
    }
}
