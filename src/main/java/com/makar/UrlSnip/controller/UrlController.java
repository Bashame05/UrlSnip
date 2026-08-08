package com.makar.UrlSnip.controller;


import com.makar.UrlSnip.dto.url.UrlAnalyticsDto;
import com.makar.UrlSnip.dto.url.UrlRequestDto;
import com.makar.UrlSnip.dto.url.UrlResponseDto;
import com.makar.UrlSnip.security.UserPrincipal;
import com.makar.UrlSnip.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class UrlController {

    private final UrlService urlService;
    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }
    @PostMapping("/api/urls")
    public ResponseEntity<UrlResponseDto> shortenUrl(@Validated @RequestBody UrlRequestDto urlRequestDto,
                                                     @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(urlService.shortenUrl(urlRequestDto,userPrincipal));
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<Void> redirect(@PathVariable String identifier, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        String longUrl = urlService.redirect(identifier);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(longUrl))
                .build();
    }

    @GetMapping("/api/urls/{identifier}/analytics")
    public ResponseEntity<UrlAnalyticsDto> analytics(@PathVariable String identifier,@AuthenticationPrincipal UserPrincipal userPrincipal){
        return ResponseEntity.ok(urlService.analytics(identifier,userPrincipal));
    }

    @GetMapping(value = "/api/urls/{identifier}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQr(@PathVariable String identifier, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(urlService.getQrCode(identifier,userPrincipal));
    }
}
