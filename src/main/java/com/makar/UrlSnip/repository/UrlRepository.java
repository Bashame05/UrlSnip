package com.makar.UrlSnip.repository;

import com.makar.UrlSnip.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<UrlMapping, Long> {
    boolean existsByShortUrlIgnoreCaseOrCustomAliasIgnoreCase(String shortUrl, String customAlias);
    Optional<UrlMapping> findByLongUrl(String longUrl);
    Optional<UrlMapping> findByShortUrlIgnoreCaseOrCustomAliasIgnoreCase(String shortUrl, String customAlias);
}
