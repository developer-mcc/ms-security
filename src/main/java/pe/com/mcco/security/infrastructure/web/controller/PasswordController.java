package pe.com.mcco.security.infrastructure.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.com.mcco.security.application.dto.ChangePasswordCommand;
import pe.com.mcco.security.application.dto.ForgotPasswordCommand;
import pe.com.mcco.security.application.dto.ResetPasswordCommand;
import pe.com.mcco.security.application.dto.TokenClaims;
import pe.com.mcco.security.domain.port.in.PasswordUseCase;
import pe.com.mcco.security.infrastructure.web.dto.request.ChangePasswordRequest;
import pe.com.mcco.security.infrastructure.web.dto.request.ForgotPasswordRequest;
import pe.com.mcco.security.infrastructure.web.dto.request.ResetPasswordRequest;
import pe.com.mcco.security.infrastructure.web.dto.response.MessageResponse;

import java.time.Duration;

/**
 * SEQ_06: POST /auth/change-password
 * SEQ_07: POST /auth/forgot-password, POST /auth/reset-password
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordUseCase passwordUseCase;

    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> cambiarPassword(
            @AuthenticationPrincipal TokenClaims claims,
            @Valid @RequestBody ChangePasswordRequest request) {

        passwordUseCase.cambiarPassword(ChangePasswordCommand.builder()
                .userId(claims.userId())
                .passwordActual(request.actual())
                .passwordNuevo(request.nuevo())
                .canal(request.canal())
                .build());

        // Clear-Cookie y forzar re-login (SEQ_06 linea 382)
        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).secure(true).path("/auth").maxAge(Duration.ZERO).build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(MessageResponse.builder()
                        .mensaje("Password actualizado. Sesion cerrada por cambio de password.")
                        .build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        passwordUseCase.solicitarReset(ForgotPasswordCommand.builder()
                .identifier(request.identifier())
                .canal(request.canal())
                .build());

        // OWASP: siempre responder 200 (SEQ_07 linea 419)
        return ResponseEntity.ok(MessageResponse.builder()
                .mensaje("Si el usuario existe, se envio un codigo al canal seleccionado")
                .build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordUseCase.resetearPassword(ResetPasswordCommand.builder()
                .token(request.token())
                .nuevaPassword(request.nuevaPassword())
                .build());

        return ResponseEntity.ok(MessageResponse.builder()
                .mensaje("Password restablecido exitosamente")
                .build());
    }
}
