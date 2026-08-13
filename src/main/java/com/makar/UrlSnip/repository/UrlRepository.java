package com.makar.UrlSnip.repository;

import com.makar.UrlSnip.model.UrlMapping;
import com.makar.UrlSnip.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<UrlMapping, Long> {
    boolean existsByShortUrlIgnoreCaseOrCustomAliasIgnoreCase(String shortUrl, String customAlias);
    Optional<UrlMapping> findByLongUrlAndUrlOwner(String longUrl, User user);
    Optional<UrlMapping> findByShortUrlIgnoreCaseOrCustomAliasIgnoreCase(String shortUrl, String customAlias);
    @Query(
            "select case when count(u) > 0 then true else false end from UrlMapping u " +
                    "where (lower(u.shortUrl) = lower(:shortUrl) or lower(u.customAlias)=lower(:customAlias))" +
                    "and u.urlOwner= :user"
    )
    boolean existsByShortUrlIgnoreCaseOrCustomAliasIgnoreCaseAndUrlOwner(String shortUrl, String customAlias, User user);
    @Query("select u from UrlMapping  u where u.isFavourite IS TRUE AND u.urlOwner = :user")
    List<UrlMapping> findAllByIsFavouriteAndUrlOwner(User user);
}
