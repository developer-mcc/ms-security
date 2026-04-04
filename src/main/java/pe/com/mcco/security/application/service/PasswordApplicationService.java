package pe.com.mcco.security.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcco.security.application.dto.ChangePasswordCommand;
import pe.com.mcco.security.application.dto.ForgotPasswordCommand;
import pe.com.mcco.security.application.dto.ResetPasswordCommand;
import pe.com.mcco.security.domain.enums.EstadoToken;
import pe.com.mcco.security.domain.enums.TipoEvento;
import pe.com.mcco.security.domain.exception.*;
import pe.com.mcco.security.domain.model.HistorialPassword;
import pe.com.mcco.security.domain.model.PasswordResetToken;
import pe.com.mcco.security.domain.model.Usuario;
import pe.com.mcco.security.domain.port.in.PasswordUseCase;
import pe.com.mcco.security.domain.port.out.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementa SEQ_06 (Cambio password) y SEQ_07 (Reset password multicanal).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PasswordApplicationService implements PasswordUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final HistorialPasswordRepositoryPort historialRepository;
    private final PasswordResetTokenRepositoryPort resetTokenRepository;
    private final TokenApplicationService tokenService;
    private final AuditApplicationService auditService;
    private final TokenCachePort tokenCache;
    private final NotificacionPort notificacion;
    private final PasswordEncoder passwordEncoder;

    private static final int HISTORIAL_LIMITE = 5;
    private static final int RESET_TOKEN_MINUTOS = 15;

    // --- SEQ_06: Cambio de password autenticado ---
    @Override
    public void cambiarPassword(ChangePasswordCommand command) {
        UUID userId = UUID.fromString(command.userId());

        // Validar fortaleza OWASP ASVS v4.0 (SEQ_06 linea 341-342)
        validarFortaleza(command.passwordNuevo(), null);

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(UsuarioNoEncontradoException::new);

        // Verificar password actual (SEQ_06 linea 350-351)
        if (!passwordEncoder.matches(command.passwordActual(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException(0);
        }

        // Verificar no repetido en historial (SEQ_06 linea 357-359)
        verificarNoRepetido(command.passwordNuevo(), userId);

        // Actualizar password (SEQ_06 linea 366-367)
        String nuevoHash = passwordEncoder.encode(command.passwordNuevo());
        usuarioRepository.updatePasswordHash(userId, nuevoHash);

        historialRepository.save(HistorialPassword.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .passwordHash(nuevoHash)
                .cambiadoEn(LocalDateTime.now())
                .build());

        // Revocar todos los tokens (SEQ_06 linea 369-373)
        tokenService.revocarTodosTokens(userId, EstadoToken.REVOCADO_PASSWORD);

        auditService.registrar(TipoEvento.PASSWORD_CAMBIADO, userId, null, null, null);

        // Notificar (SEQ_06 linea 378-379)
        String destino = usuario.getEmail() != null ? usuario.getEmail() : usuario.getCelular();
        if (destino != null) {
            notificacion.notificar(destino, command.canal(), "Password cambiado exitosamente");
        }
    }

    // --- SEQ_07 Paso 1: Solicitar codigo de reset ---
    @Override
    public void solicitarReset(ForgotPasswordCommand command) {
        // OWASP: siempre responder 200, no revelar si existe (SEQ_07 linea 419)
        usuarioRepository.findByEmailOrCelular(command.identifier()).ifPresent(usuario -> {
            String otp = generarOTP();

            resetTokenRepository.save(PasswordResetToken.builder()
                    .id(UUID.randomUUID())
                    .userId(usuario.getId())
                    .token(otp)
                    .canal(command.canal())
                    .expiraEn(LocalDateTime.now().plusMinutes(RESET_TOKEN_MINUTOS))
                    .usado(false)
                    .build());

            // Guardar en cache con TTL (SEQ_07 linea 424)
            tokenCache.guardarResetToken(otp, usuario.getId().toString(),
                    (long) RESET_TOKEN_MINUTOS * 60);

            String destino = command.canal().name().equals("EMAIL")
                    ? usuario.getEmail() : usuario.getCelular();
            notificacion.enviarCodigo(destino, command.canal(), otp);

            auditService.registrar(TipoEvento.PASSWORD_RESET_SOLICITADO,
                    usuario.getId(), null, null, "canal=" + command.canal());
        });
    }

    // --- SEQ_07 Paso 2: Verificar codigo y nueva password ---
    @Override
    public void resetearPassword(ResetPasswordCommand command) {
        // Verificar token en cache (SEQ_07 linea 448)
        String userId = tokenCache.obtenerResetToken(command.token())
                .orElseThrow(() -> new TokenInvalidoException("token de reset invalido o expirado"));

        // Verificar en BD que no haya sido usado (SEQ_07 linea 455)
        PasswordResetToken resetToken = resetTokenRepository.findByTokenAndNoUsado(command.token())
                .orElseThrow(() -> new TokenInvalidoException("token ya utilizado"));

        // Validar fortaleza (SEQ_07 linea 462)
        validarFortaleza(command.nuevaPassword(), null);

        UUID uid = UUID.fromString(userId);

        // Actualizar password (SEQ_07 linea 469)
        String nuevoHash = passwordEncoder.encode(command.nuevaPassword());
        usuarioRepository.updatePasswordHash(uid, nuevoHash);

        // Marcar token como usado (SEQ_07 linea 470)
        resetTokenRepository.marcarUsado(resetToken.getId());
        tokenCache.eliminarResetToken(command.token());

        // Revocar tokens activos (SEQ_07 linea 473-476)
        tokenService.revocarTodosTokens(uid, EstadoToken.REVOCADO_PASSWORD);

        auditService.registrar(TipoEvento.PASSWORD_RESET_OK, uid, null, null, null);
    }

    // --- Validacion OWASP ASVS v4.0 (SEQ_06 linea 342) ---
    private void validarFortaleza(String password, String username) {
        List<String> errores = new ArrayList<>();

        if (password.length() < 12) {
            errores.add("Minimo 12 caracteres");
        }
        if (!password.matches(".*[A-Z].*")) {
            errores.add("Debe contener al menos una mayuscula");
        }
        if (!password.matches(".*[a-z].*")) {
            errores.add("Debe contener al menos una minuscula");
        }
        if (!password.matches(".*\\d.*")) {
            errores.add("Debe contener al menos un numero");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            errores.add("Debe contener al menos un caracter especial");
        }
        if (username != null && password.toLowerCase().contains(username.toLowerCase())) {
            errores.add("No puede contener el nombre de usuario");
        }

        if (!errores.isEmpty()) {
            throw new PasswordDebilException(errores);
        }
    }

    private void verificarNoRepetido(String passwordNuevo, UUID userId) {
        List<HistorialPassword> historial = historialRepository.findRecientesByUserId(userId, HISTORIAL_LIMITE);
        for (HistorialPassword h : historial) {
            if (passwordEncoder.matches(passwordNuevo, h.getPasswordHash())) {
                throw new PasswordRepetidoException();
            }
        }
    }

    private String generarOTP() {
        return String.format("%06d", new SecureRandom().nextInt(999999));
    }
}
