package com.toie.securedRestApiTutorial.service;

import com.toie.securedRestApiTutorial.domain.LoginRequestDto;
import com.toie.securedRestApiTutorial.domain.LoginResponseDto;
import com.toie.securedRestApiTutorial.domain.RegisterRequestDto;
import com.toie.securedRestApiTutorial.domain.RegisterResponseDto;
import com.toie.securedRestApiTutorial.mapper.UserMapper;
import com.toie.securedRestApiTutorial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public RegisterResponseDto register(RegisterRequestDto registerRequestDto) {
        var user = userMapper.map(registerRequestDto, UUID.randomUUID(), passwordEncoder.encode(registerRequestDto.getPassword()));
        userRepository.save(user);

        var token = jwtService.generateToken(user);

        return RegisterResponseDto.builder().token(token).build();
    }

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                ));
        var user = userRepository.findByEmail(loginRequestDto.getEmail()).orElseThrow();

        var token = jwtService.generateToken(user);

        return LoginResponseDto.builder().token(token).build();
    }
}
