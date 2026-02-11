package com.epam.finaltask.service.impl;

import com.epam.finaltask.dto.*;
import com.epam.finaltask.exception.ExpiredTokenException;
import com.epam.finaltask.exception.InvalidTokenException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.AuthenticationService;
import com.epam.finaltask.service.TokenStorageService;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final TokenStorageService<String> jwtTokenStorageService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        ));

        User user = userMapper.toUser(userService.getUserByUsername(loginRequest.getUsername()));
        return generateTokensAndStore(user);
    }

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        User user = User.builder()
                .username(registerRequest.getUsername())
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .role(Role.USER)
                .active(true)
                .build();

        User newUser = userMapper.toUser(userService.saveUser(
                userMapper.toUserDTO(user),
                passwordEncoder.encode(registerRequest.getPassword())
        ));

        return generateTokensAndStore(newUser);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();
        String username = jwtUtil.extractUsername(refreshToken);

        if (jwtTokenStorageService.get(username) == null) {
            throw new InvalidTokenException();
        }
        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new ExpiredTokenException();
        }

        User user = userMapper.toUser(
                userService.getUserById(
                        UUID.fromString(
                                jwtUtil.extractClaim(refreshToken, claims -> claims.get("id", String.class))
                        )
                )
        );

        return generateTokensAndStore(user);
    }

    @Override
    public void logout(LogoutRequest logoutRequest) {
        String id = jwtUtil.extractAllClaims(logoutRequest.getRefreshToken()).get("id", String.class);

        if (id != null) {
            jwtTokenStorageService.revoke(id);
        }

        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest resetPasswordRequest, User user) {
        User currentUser = userMapper.toUser(userService.getUserByUsername(user.getUsername()));

        userService.changePassword(
                userMapper.toUserDTO(currentUser),
                passwordEncoder.encode(resetPasswordRequest.getNewPassword())
        );

        jwtTokenStorageService.revoke(currentUser.getId().toString());

    }

    @Override
    public AuthResponse generateTokensAndStore(User user) {
        AuthResponse authResponse = generateTokens(user);

        jwtTokenStorageService.revoke(user.getId().toString());
        jwtTokenStorageService.store(user.getId().toString(), authResponse.getRefreshToken());

        return authResponse;
    }

    private AuthResponse generateTokens(User user) {
        String jwtToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }
}