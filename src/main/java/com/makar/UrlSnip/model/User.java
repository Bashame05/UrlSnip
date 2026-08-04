package com.makar.UrlSnip.model;

import com.makar.UrlSnip.utils.ROLES;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "user")
public class User {
    @Id
    @UuidGenerator
    private UUID userId;
    private String userName;
    private String userPassword;
    private String  userEmail;
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    private ROLES userRole;
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "urlOwner")
    private List<UrlMapping> userOwnedUrls;
}
