package com.makar.UrlSnip.controller;


import com.makar.UrlSnip.dto.url.*;
import com.makar.UrlSnip.ratelimit.RateLimitConfig;
import com.makar.UrlSnip.security.UserPrincipal;
import com.makar.UrlSnip.service.UrlService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
public class UrlController {

    private final UrlService urlService;
    private final RateLimitConfig rateLimitConfig;
    public UrlController(UrlService urlService, RateLimitConfig rateLimitConfig) {
        this.urlService = urlService;
        this.rateLimitConfig = rateLimitConfig;
    }
    @PostMapping("/api/urls")
    public ResponseEntity<UrlResponseDto> shortenUrl(@Validated @RequestBody UrlRequestDto urlRequestDto,
                                                     @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(urlService.shortenUrl(urlRequestDto,userPrincipal));
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<Void> redirect(@PathVariable String identifier , HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if(clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }else{
            clientIp = clientIp.split(",")[0].trim();
        }
        Bucket bucket = rateLimitConfig.getBucket(clientIp);
        if(bucket.tryConsume(1)) {
            String longUrl = urlService.redirect(identifier);
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .location(URI.create(longUrl))
                    .build();
        }else{
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .build();
        }
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

    @PostMapping("/api/url/{identifier}/favourites")
    public ResponseEntity<String> markUrlAsFavourite(@PathVariable String identifier, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        urlService.markUrlAsFavourite(identifier,userPrincipal);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Url marked as favourite");
    }

    @GetMapping("/api/url/favourites")
    public ResponseEntity<List<UserFavouriteUrlDto>> getUserFavouriteUrl(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(urlService.getUserFavouriteUrls(userPrincipal));
    }

    @GetMapping("/api/user/analytics")
    public ResponseEntity<UserAnalyticsDto>  getUserAnalytics(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(urlService.getUserAnalytics(userPrincipal));
    }
}
