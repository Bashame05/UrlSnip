package com.makar.urlshortner.repository;

import com.makar.urlshortner.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepository extends JpaRepository<UrlMapping, Long> {
    boolean existsByShortUrl(String shortUrl);
    boolean existsByLongUrl(String longUrl);
    UrlMapping findByLongUrl(String longUrl);
}
