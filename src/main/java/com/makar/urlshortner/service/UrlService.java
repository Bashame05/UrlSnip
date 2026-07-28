package com.makar.urlshortner.service;


import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.makar.urlshortner.dto.UrlResponseDto;
import com.makar.urlshortner.mapper.UrlResponseMapper;
import com.makar.urlshortner.model.UrlMapping;
import com.makar.urlshortner.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UrlService {
    private final String absoluteUrl;
    private final UrlRepository urlRepository;
    private final UrlResponseMapper urlResponseMapper;
    public UrlService(UrlRepository urlRepository, UrlResponseMapper urlResponseMapper, @Value("${ABSOLUTE_URL}") String absoluteUrl) {
        this.urlRepository = urlRepository;
        this.urlResponseMapper = urlResponseMapper;
        this.absoluteUrl = absoluteUrl;
    }

    public UrlResponseDto shortenUrl(String longUrl){
        if(urlRepository.existsByLongUrl(longUrl)){
            return urlResponseMapper.apply(urlRepository.findByLongUrl(longUrl));
        }
        //write the isPresent method to optimize the longUrl check
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setLongUrl(longUrl);
        urlMapping.setShortUrl(getUniqueShortUrl());
        urlMapping.setClickCount(0L);
        urlMapping.setCreatedAt(LocalDateTime.now());
        urlMapping.setLastAccessed(null);
        return urlResponseMapper.apply(urlRepository.save(urlMapping));
    }

    private String getUniqueShortUrl(){
        String shortUrl = generateShortUrl();
        while(urlRepository.existsByShortUrl(shortUrl)){
            shortUrl = generateShortUrl();
        }
        return shortUrl;
    }
    private String generateShortUrl() {
        StringBuilder shortUrl = new StringBuilder(absoluteUrl);
        String nanoId = NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR,NanoIdUtils.DEFAULT_ALPHABET,7);
        shortUrl.append(nanoId);
        return shortUrl.toString();
    }

}
