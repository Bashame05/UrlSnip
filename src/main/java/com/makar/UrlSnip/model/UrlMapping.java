package com.makar.UrlSnip.model;

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
    @Column(nullable = false)
    private String longUrl;
    @Column(unique = true)
    private String customAlias;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessed;
    private LocalDateTime expiresAt;
    private Long clickCount;
    @Column(nullable = false)
    private boolean isFavourite = false;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User urlOwner;

}
