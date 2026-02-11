package com.epam.finaltask.contoller;

import com.epam.finaltask.dto.*;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.AttemptService;
import com.epam.finaltask.service.AuthenticationService;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.util.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtProperties jwtProperties;
    private final UserService userService;
    private final AttemptService attemptService;

    @GetMapping("/sign-in")
    public String signIn(@RequestParam(value = "error", required = false) String error,
                         @ModelAttribute("loginRequest") LoginRequest loginRequest,
                         Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }

        return "auth/sign-in";
    }

    @GetMapping("/sign-up")
    public String signUp(@ModelAttribute("registerRequest") RegisterRequest registerRequest) {
        return "auth/sign-up";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("registerRequest") @Valid RegisterRequest registerRequest,
                           BindingResult bindingResult,
                           HttpServletResponse response,
                           Model model) {

        if (bindingResult.hasErrors()) {
            response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
            return "auth/sign-up :: signup-form";
        }

        AuthResponse authResponse = authenticationService.register(registerRequest);

        saveTokensToCookies(response, authResponse.getAccessToken(), authResponse.getRefreshToken());

        response.setHeader("HX-Redirect", "/index");
        return null;
    }

    @PostMapping("/perform_login")
    public String login(@ModelAttribute("loginRequest") @Valid LoginRequest loginRequest,
                        BindingResult bindingResult,
                        HttpServletResponse response,
                        HttpServletRequest request,
                        Model model) {

        if (bindingResult.hasErrors()) {
            response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
            return "auth/sign-in :: login-form";
        }

        try {
            AuthResponse authResponse = authenticationService.login(loginRequest);

            attemptService.clearBlocked(getClientIP(request));

            saveTokensToCookies(response, authResponse.getAccessToken(), authResponse.getRefreshToken());
            response.setHeader("HX-Redirect", "/index");
            return null;

        } catch (AuthenticationException e) {
            String ip = getClientIP(request);
            attemptService.track(ip);

            throw e;
        }
    }

    @GetMapping("/reset-password-form")
    public String getResetForm(@AuthenticationPrincipal User user, Model model) {
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        model.addAttribute("resetPasswordRequest", resetPasswordRequest);

        return "fragments/reset-password :: reset-password-fragment";
    }

    @PostMapping("/reset-password")
    public String requestReset(@ModelAttribute("resetPasswordRequest") @Valid ResetPasswordRequest request,
                               @AuthenticationPrincipal User user,
                               BindingResult bindingResult,
                               HttpServletResponse response,
                               Model model) {


        if (bindingResult.hasErrors()) {

            response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());

            model.addAttribute("validToken", true);

            return "auth/reset-password :: reset-password-final";
        }
        return "auth/reset-password :: reset-password-final";
    }

    @GetMapping("/reset-password/validate")
    public String showResetForm(Model model) {
        ResetPasswordRequest request = new ResetPasswordRequest();

        model.addAttribute("validToken", true);
        model.addAttribute("resetPasswordRequest", request);

        return "auth/reset-password";
    }

    @PostMapping("/reset-password/confirm")
    public String confirmReset(@ModelAttribute("resetPasswordRequest") @Valid ResetPasswordRequest request,
                               BindingResult bindingResult,
                               @AuthenticationPrincipal User user,
                               HttpServletResponse response,
                               Model model) {

        if (bindingResult.hasErrors()) {
            response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
            return "fragments/reset-password :: reset-password-fragment";
        }

        authenticationService.resetPassword(request, user);

        model.addAttribute("success", true);
        model.addAttribute("message", "Password changed successfully!");
        return "fragments/reset-password :: reset-password-fragment";

    }

    private void saveTokensToCookies(HttpServletResponse response,
                                     String access,
                                     String refresh) {

        Cookie aCookie = new Cookie("jwt_access", access);
        aCookie.setHttpOnly(true);
        aCookie.setPath("/");
        aCookie.setMaxAge((int) jwtProperties.getExpiration());

        Cookie rCookie = new Cookie("jwt_refresh", refresh);
        rCookie.setHttpOnly(true);
        rCookie.setPath("/auth/refresh");
        rCookie.setMaxAge((int) jwtProperties.getRefreshToken().getExpiration());

        response.addCookie(aCookie);
        response.addCookie(rCookie);
    }

    private void resetCookies(HttpServletResponse response) {

        Cookie aCookie = new Cookie("jwt_access", null);
        aCookie.setPath("/");
        aCookie.setMaxAge(0);

        Cookie rCookie = new Cookie("jwt_refresh", null);
        rCookie.setPath("/");
        rCookie.setMaxAge(0);

        response.addCookie(aCookie);
        response.addCookie(rCookie);
    }

    private String getClientIP(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .map(h -> h.split(",")[0].trim())
                .orElse(request.getRemoteAddr());
    }
}
