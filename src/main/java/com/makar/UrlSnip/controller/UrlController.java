package com.makar.UrlSnip.controller;


import com.makar.UrlSnip.dto.UrlAnalyticsDto;
import com.makar.UrlSnip.dto.UrlRequestDto;
import com.makar.UrlSnip.dto.UrlResponseDto;
import com.makar.UrlSnip.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class UrlController {

    private final UrlService urlService;
    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }
    @PostMapping("/api/urls")
    public ResponseEntity<UrlResponseDto> shortenUrl(@RequestBody UrlRequestDto urlRequestDto) {
        return ResponseEntity.ok(urlService.shortenUrl(urlRequestDto));
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl){
        String longUrl = urlService.redirect(shortUrl);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(longUrl))
                .build();
    }

    @GetMapping("/api/urls/{shortUrl}/analytics")
    public ResponseEntity<UrlAnalyticsDto> analytics(@PathVariable String shortUrl){
        return ResponseEntity.ok(urlService.analytics(shortUrl));
    }
}
