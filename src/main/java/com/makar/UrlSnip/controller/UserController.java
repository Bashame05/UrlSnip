package com.makar.UrlSnip.controller;


import com.makar.UrlSnip.dto.auth.UserLoginDto;
import com.makar.UrlSnip.dto.auth.UserRegisterDto;
import com.makar.UrlSnip.dto.auth.UserResponseDto;
import com.makar.UrlSnip.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/api/auth/register")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserRegisterDto userRegisterDto) {
        return ResponseEntity.ok(userService.registerUser(userRegisterDto));
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<String> login(@Valid @RequestBody UserLoginDto userLoginDto){
        return ResponseEntity.ok(userService.login(userLoginDto));
    }
}
