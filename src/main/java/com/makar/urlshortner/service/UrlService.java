package com.makar.urlshortner.service;


import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.makar.urlshortner.dto.UrlRequestDto;
import com.makar.urlshortner.dto.UrlResponseDto;
import com.makar.urlshortner.exception.NoSuchUrlException;
import com.makar.urlshortner.mapper.UrlResponseMapper;
import com.makar.urlshortner.model.UrlMapping;
import com.makar.urlshortner.repository.UrlRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UrlService {
    private final UrlRepository urlRepository;
    private final UrlResponseMapper urlResponseMapper;
    public UrlService(UrlRepository urlRepository, UrlResponseMapper urlResponseMapper) {
        this.urlRepository = urlRepository;
        this.urlResponseMapper = urlResponseMapper;
    }

    public UrlResponseDto shortenUrl(UrlRequestDto urlRequestDto) {
        Optional<UrlMapping> urlMapping = findExistingUrl(urlRequestDto.longUrl());
        if(urlMapping.isPresent()){
            return urlResponseMapper.apply(urlMapping.get());
        }
        UrlMapping newUrl = new UrlMapping();
        newUrl.setLongUrl(urlRequestDto.longUrl());
        newUrl.setShortUrl(getUniqueShortUrl());
        newUrl.setClickCount(0L);
        newUrl.setCreatedAt(LocalDateTime.now());
        newUrl.setLastAccessed(null);
        return urlResponseMapper.apply(urlRepository.save(newUrl));
    }

    private String getUniqueShortUrl(){
        String shortUrl = generateShortUrl();
        while(urlRepository.existsByShortUrl(shortUrl)){
            shortUrl = generateShortUrl();
        }
        return shortUrl;
    }
    private String generateShortUrl() {
        String shortUrl = NanoIdUtils
                .randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR,NanoIdUtils.DEFAULT_ALPHABET,7);
        return shortUrl;
    }

    public Optional<UrlMapping> findExistingUrl(String longUrl){
        return urlRepository.findByLongUrl(longUrl);
    }

    public String redirect(String shortUrl){
        UrlMapping url = urlRepository.findByShortUrl(shortUrl)
                .orElseThrow(()-> new NoSuchUrlException("No such url found"));
        url.setLastAccessed(LocalDateTime.now());
        url.setClickCount(url.getClickCount()+1);
        return urlRepository.save(url).getLongUrl();
    }
}
