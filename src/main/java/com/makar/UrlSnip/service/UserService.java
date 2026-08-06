package com.makar.UrlSnip.service;


import com.makar.UrlSnip.dto.UserRegisterDto;
import com.makar.UrlSnip.dto.UserResponseDto;
import com.makar.UrlSnip.exception.UserAlreadyExistsException;
import com.makar.UrlSnip.mapper.UserResponseMapper;
import com.makar.UrlSnip.model.User;
import com.makar.UrlSnip.repository.UserRepository;
import com.makar.UrlSnip.utils.ROLES;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;
    public UserService(UserRepository userRepository, UserResponseMapper userResponseMapper) {
        this.userRepository = userRepository;
        this.userResponseMapper = userResponseMapper;
    }

    public UserResponseDto registerUser(UserRegisterDto userRegisterDto) {
        if(userRepository.existsByUserNameOrUserEmail(userRegisterDto.userName(), userRegisterDto.userEmail())) {
            throw new UserAlreadyExistsException("A User with these credentials already exists");
        }
        User userToRegister = new User();
        userToRegister.setUserName(userRegisterDto.userName());
        userToRegister.setUserPassword(userRegisterDto.userPassword());
        userToRegister.setUserEmail(userRegisterDto.userEmail());
        userToRegister.setCreatedAt(LocalDateTime.now());
        userToRegister.setUserRole(ROLES.USER);
        return userResponseMapper.apply(userRepository.save(userToRegister));
    }

}
