package com.toie.securedRestApiTutorial.controller;

import com.toie.securedRestApiTutorial.domain.LoginRequestDto;
import com.toie.securedRestApiTutorial.domain.LoginResponseDto;
import com.toie.securedRestApiTutorial.domain.RegisterRequestDto;
import com.toie.securedRestApiTutorial.domain.RegisterResponseDto;
import com.toie.securedRestApiTutorial.service.AuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        var loginResponse = authenticationService.login(loginRequestDto);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody RegisterRequestDto registerRequestDto) {
        var registerResponse = authenticationService.register(registerRequestDto);
        return ResponseEntity.ok(registerResponse);
    }
}
