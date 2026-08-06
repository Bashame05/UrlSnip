package com.makar.UrlSnip.service;


import com.makar.UrlSnip.dto.UserLoginDto;
import com.makar.UrlSnip.dto.UserRegisterDto;
import com.makar.UrlSnip.dto.UserResponseDto;
import com.makar.UrlSnip.exception.UserAlreadyExistsException;
import com.makar.UrlSnip.mapper.UserResponseMapper;
import com.makar.UrlSnip.model.User;
import com.makar.UrlSnip.repository.UserRepository;
import com.makar.UrlSnip.utils.ROLES;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepository, UserResponseMapper userResponseMapper , PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.userResponseMapper = userResponseMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public UserResponseDto registerUser(UserRegisterDto userRegisterDto) {
        if(userRepository.existsByUserNameIgnoreCaseOrUserEmail(userRegisterDto.userName(), userRegisterDto.userEmail())) {
            throw new UserAlreadyExistsException("A User with these credentials already exists");
        }
        User userToRegister = new User();
        userToRegister.setUserName(userRegisterDto.userName());
        userToRegister.setUserPassword(passwordEncoder.encode(userRegisterDto.userPassword()));
        userToRegister.setUserEmail(userRegisterDto.userEmail());
        userToRegister.setCreatedAt(LocalDateTime.now());
        userToRegister.setUserRole(ROLES.USER);
        return userResponseMapper.apply(userRepository.save(userToRegister));
    }

    public String login(UserLoginDto userLoginDto) {
        try{
            Authentication authentication  = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userLoginDto.userName(),userLoginDto.userPassword()));
            return "jwt";
        }catch (BadCredentialsException e){
            return e.getMessage();
        }
    }
}
