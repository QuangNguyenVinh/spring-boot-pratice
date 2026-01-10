package com.example.sbp.controller;

import com.example.sbp.dto.TokenDTO;
import com.example.sbp.security.model.UserPrincipal;
import com.example.sbp.service.AppUserService;
import com.example.sbp.service.SessionService;
import com.example.sbp.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationManager authManager;
    private final AppUserService appUserService;
    private final SessionService sessionService;
    private final TokenService tokenService;

    public AuthenticationController(AuthenticationManager authManager, AppUserService appUserService,
                                    SessionService sessionService, TokenService tokenService) {
        this.authManager = authManager;
        this.appUserService = appUserService;
        this.sessionService = sessionService;
        this.tokenService = tokenService;
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password, String deviceId) {
    }
    public record RefreshRequest(String refreshToken) {}

    @PostMapping("/login")
    public TokenDTO login(@RequestBody LoginRequest req, HttpServletRequest request) throws NoSuchAlgorithmException, UnknownHostException {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");

        var session = sessionService.create(principal.getUserId(), ip, ua, req.deviceId());

        return tokenService.issueOnLogin(principal, session);
    }

    @PostMapping("/refresh")
    public TokenDTO refresh(@RequestBody RefreshRequest req) throws Exception {
        return tokenService.rotateRefresh(req.refreshToken());
    }
}
