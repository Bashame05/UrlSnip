package com.makar.urlshortner.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "url_mapping")
public class UrlMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long urlId;
    @Column(unique = true, nullable = false)
    private String shortUrl;
    @Column(unique = true, nullable = false)
    private String longUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessed;
    private Long clickCount;

}
