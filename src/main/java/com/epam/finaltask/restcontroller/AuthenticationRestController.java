package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.*;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.AuthenticationService;
import com.epam.finaltask.service.ResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationRestController {

    private final AuthenticationService authenticationService;

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> signUp(@RequestBody @Valid RegisterRequest registerRequest) {
        return ResponseEntity.ok().body(authenticationService.register(registerRequest));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.ok().body(authenticationService.login(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok().body(authenticationService.refresh(refreshTokenRequest));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> logout(@RequestBody @Valid LogoutRequest logoutRequest) {
        authenticationService.logout(logoutRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<AuthResponse> oauth2Success(
            @RequestParam("accessToken") String accessToken,
            @RequestParam("refreshToken") String refreshToken
    ) {
        return ResponseEntity.ok(
                AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build()
        );
    }


    @GetMapping("/reset-password")
    public ResponseEntity<String> validateToken() {
        return ResponseEntity.ok("Token is valid");
    }

    @PostMapping("/reset-password/confirm")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request, @AuthenticationPrincipal User user) {
        authenticationService.resetPassword(request, user);

        return ResponseEntity.ok("Password updated");
    }
}
