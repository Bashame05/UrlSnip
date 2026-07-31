package com.makar.UrlSnip.service;


import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.makar.UrlSnip.dto.UrlAnalyticsDto;
import com.makar.UrlSnip.dto.UrlRequestDto;
import com.makar.UrlSnip.dto.UrlResponseDto;
import com.makar.UrlSnip.exception.AliasNotAllowedException;
import com.makar.UrlSnip.exception.NoSuchUrlException;
import com.makar.UrlSnip.exception.UrlExpiredException;
import com.makar.UrlSnip.mapper.UrlAnalyticsMapper;
import com.makar.UrlSnip.mapper.UrlResponseMapper;
import com.makar.UrlSnip.model.UrlMapping;
import com.makar.UrlSnip.repository.UrlRepository;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UrlService {
    private final UrlRepository urlRepository;
    private final UrlResponseMapper urlResponseMapper;
    public final UrlAnalyticsMapper urlAnalyticsMapper;

    public UrlService(UrlRepository urlRepository,
                      UrlResponseMapper urlResponseMapper,
                      UrlAnalyticsMapper urlAnalyticsMapper) {
        this.urlRepository = urlRepository;
        this.urlResponseMapper = urlResponseMapper;
        this.urlAnalyticsMapper = urlAnalyticsMapper;
    }

    private final String regex = "^[a-zA-Z0-9]+$";
    private final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

    private final List<String> prohibitedAliases = List.of(
            "urls","api","health","analytics","docs","swagger","swaggerui","actuator","error","favicon.ico","robots.txt");

    private final int QR_WIDTH = 300;
    private final int QR_HEIGHT = 300;

    public UrlResponseDto shortenUrl(UrlRequestDto urlRequestDto) {
        Optional<UrlMapping> urlMapping = findExistingUrl(urlRequestDto.longUrl());
        if (urlMapping.isPresent()) {
            return urlResponseMapper.apply(urlMapping.get());
        }
        UrlMapping newUrl = new UrlMapping();
        newUrl.setLongUrl(urlRequestDto.longUrl());
        if(urlRequestDto.customAlias() != null) {
            newUrl.setCustomAlias(checkIfAliasIsUnique(urlRequestDto.customAlias()));
        }
        if(urlRequestDto.expiresInDays() != null && urlRequestDto.expiresInDays() > 0) {
            newUrl.setExpiresAt(LocalDateTime.now().plusDays(urlRequestDto.expiresInDays()));
        }
        newUrl.setShortUrl(getUniqueShortUrl());
        newUrl.setClickCount(0L);
        newUrl.setCreatedAt(LocalDateTime.now());
        newUrl.setLastAccessed(null);
        return urlResponseMapper.apply(urlRepository.save(newUrl));
    }

    private String checkIfAliasIsUnique(String customAlias) {
        customAlias = customAlias.toLowerCase().trim();
        if(customAlias.isBlank() || customAlias.length()<3 || customAlias.length()>7) {
            throw new AliasNotAllowedException("alias length must be between 3 and 7");
        }
        if(prohibitedAliases.contains(customAlias)){
            throw new AliasNotAllowedException("Alias provided is prohibited to use");
        }
        if(!pattern.matcher(customAlias).matches()) {
            throw new AliasNotAllowedException("Alias can only contain numbers or letters");
        }
        if(urlRepository.existsByShortUrlIgnoreCaseOrCustomAliasIgnoreCase(customAlias,customAlias)) {
            throw new AliasNotAllowedException("Alias is unavailable");
        }
        return customAlias;
    }

    private String getUniqueShortUrl() {
        String shortUrl = generateShortUrl();
        while (urlRepository.existsByShortUrlIgnoreCaseOrCustomAliasIgnoreCase(shortUrl,shortUrl)) {
            shortUrl = generateShortUrl();
        }
        return shortUrl;
    }

    private String generateShortUrl() {
        String shortUrl = NanoIdUtils
                .randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, NanoIdUtils.DEFAULT_ALPHABET, 7);
        return shortUrl;
    }

    private Optional<UrlMapping> findExistingUrl(String longUrl) {
        return urlRepository.findByLongUrl(longUrl);
    }

    public String redirect(String identifier) {
        UrlMapping url = findByIdentifier(identifier);
        if(url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException("URL has expired");
        }
        url.setLastAccessed(LocalDateTime.now());
        url.setClickCount(url.getClickCount() + 1);
        return urlRepository.save(url).getLongUrl();
    }

    public UrlAnalyticsDto analytics(String identifier) {
        UrlMapping url = findByIdentifier(identifier);
        return urlAnalyticsMapper.apply(url);
    }

    private UrlMapping findByIdentifier(String identifier) {
        UrlMapping urlMapping = urlRepository.findByShortUrlIgnoreCaseOrCustomAliasIgnoreCase(identifier,identifier)
                .orElseThrow(() -> new NoSuchUrlException("No such URL or alias found"));
        return urlMapping;
    }
    
    public byte[] getQrCode(String identifier) {
        UrlMapping url = findByIdentifier(identifier);
        if(url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException("URL has expired");
        }
        url.setLastAccessed(LocalDateTime.now());
        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);
        return generateQrCode(url.getLongUrl());
    }

    private byte[] generateQrCode(String longUrl) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = qrCodeWriter.encode(longUrl, BarcodeFormat.QR_CODE, QR_WIDTH,QR_HEIGHT);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage,"png",byteArrayOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
