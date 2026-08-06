package com.makar.UrlSnip.mapper;

import com.makar.UrlSnip.dto.UserResponseDto;
import com.makar.UrlSnip.model.User;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UserResponseMapper implements Function<User,UserResponseDto> {
    @Override
    public UserResponseDto apply(User user) {
        return new UserResponseDto(
                user.getUserId(),
                user.getUserName(),
                user.getUserEmail(),
                user.getUserRole().toString(),
                user.getCreatedAt().toString()
        );
    }
}
