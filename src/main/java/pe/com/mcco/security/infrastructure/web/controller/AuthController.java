package pe.com.mcco.security.infrastructure.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.com.mcco.security.application.dto.AuthResult;
import pe.com.mcco.security.application.dto.LoginCommand;
import pe.com.mcco.security.application.dto.TokenClaims;
import pe.com.mcco.security.application.dto.RegisterUserCommand;
import pe.com.mcco.security.domain.model.Usuario;
import pe.com.mcco.security.domain.port.in.AuthUseCase;
import pe.com.mcco.security.domain.port.in.UsuarioUseCase;
import pe.com.mcco.security.infrastructure.web.dto.request.LoginRequest;
import pe.com.mcco.security.infrastructure.web.dto.request.RegisterRequest;
import pe.com.mcco.security.infrastructure.web.dto.response.AuthResponse;
import pe.com.mcco.security.infrastructure.web.dto.response.MessageResponse;
import pe.com.mcco.security.infrastructure.web.dto.response.UserCreatedResponse;

import java.time.Duration;

/**
 * SEQ_01: POST /auth/login
 * SEQ_03: POST /auth/logout
 * SEQ_04: POST /auth/refresh
 * SEQ_05: POST /auth/logout?reason=idle
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;
    private final UsuarioUseCase usuarioUseCase;

    @PostMapping("/register")
    public ResponseEntity<UserCreatedResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        Usuario usuario = usuarioUseCase.registrar(RegisterUserCommand.builder()
                .username(request.username())
                .password(request.password())
                .email(request.email())
                .celular(request.celular())
                .build());

        return ResponseEntity.status(201)
                .body(UserCreatedResponse.builder()
                        .id(usuario.getId())
                        .username(usuario.getUsername())
                        .email(usuario.getEmail())
                        .mensaje("Usuario registrado exitosamente")
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        AuthResult result = authUseCase.login(LoginCommand.builder()
                .username(request.username())
                .password(request.password())
                .branchId(request.branchId())
                .ip(httpRequest.getRemoteAddr())
                .build());

        // Set-Cookie: refreshToken httpOnly Secure (SEQ_01 linea 92)
        ResponseCookie cookie = buildRefreshCookie(result.refreshToken(), Duration.ofDays(7));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(AuthResponse.builder()
                        .accessToken(result.accessToken())
                        .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @AuthenticationPrincipal TokenClaims claims,
            @RequestParam(required = false) String reason,
            HttpServletRequest httpRequest) {

        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        long expSeconds = System.currentTimeMillis() / 1000 + 900; // fallback 15m

        if ("idle".equals(reason)) {
            authUseCase.logoutByIdle(claims.jti(), expSeconds, claims.userId(), ip, userAgent);
        } else {
            authUseCase.logout(claims.jti(), expSeconds, claims.userId(), ip, userAgent);
        }

        // Clear-Cookie: refreshToken (SEQ_03 linea 205)
        ResponseCookie clearCookie = buildRefreshCookie("", Duration.ZERO);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(MessageResponse.builder()
                        .loggedOut(true)
                        .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue("refreshToken") String refreshToken) {

        AuthResult result = authUseCase.refresh(refreshToken);

        ResponseCookie cookie = buildRefreshCookie(result.refreshToken(), Duration.ofDays(7));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(AuthResponse.builder()
                        .accessToken(result.accessToken())
                        .build());
    }

    private ResponseCookie buildRefreshCookie(String value, Duration maxAge) {
        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .secure(true)
                .path("/auth")
                .maxAge(maxAge)
                .sameSite("Strict")
                .build();
    }
}
