package pe.com.mcco.security.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcco.security.application.dto.AuthResult;
import pe.com.mcco.security.application.dto.LoginCommand;
import pe.com.mcco.security.application.dto.TokenClaims;
import pe.com.mcco.security.domain.enums.EstadoToken;
import pe.com.mcco.security.domain.enums.TipoEvento;
import pe.com.mcco.security.domain.exception.CredencialesInvalidasException;
import pe.com.mcco.security.domain.exception.UsuarioNoEncontradoException;
import pe.com.mcco.security.domain.model.Usuario;
import pe.com.mcco.security.domain.port.in.AuthUseCase;
import pe.com.mcco.security.domain.port.out.UsuarioRepositoryPort;

import java.util.UUID;

/**
 * Implementa SEQ_01 (Login), SEQ_02 (Validacion), SEQ_03 (Logout),
 * SEQ_04 (Refresh), SEQ_05 (Inactividad).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthApplicationService implements AuthUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final TokenApplicationService tokenService;
    private final LoginAttemptApplicationService loginAttemptService;
    private final AuditApplicationService auditService;
    private final PasswordEncoder passwordEncoder;

    // --- SEQ_01: Login con reintentos OWASP ---
    @Override
    public AuthResult login(LoginCommand command) {
        // Verificar si esta bloqueado (SEQ_01 linea 33-34)
        loginAttemptService.verificarBloqueo(command.username());

        // Buscar usuario activo (SEQ_01 linea 44)
        Usuario usuario = usuarioRepository.findByUsernameAndActivo(command.username())
                .orElseThrow(() -> {
                    // OWASP: mismo mensaje para user inexistente y password incorrecto (linea 50)
                    loginAttemptService.registrarIntento(command.username(), command.ip(), false);
                    loginAttemptService.bloquearSiExcedeMaximo(command.username());
                    return new CredencialesInvalidasException(
                            loginAttemptService.intentosRestantes(command.username()));
                });

        // Verificar password con BCrypt (SEQ_01 linea 54)
        if (!passwordEncoder.matches(command.password(), usuario.getPasswordHash())) {
            loginAttemptService.registrarIntento(command.username(), command.ip(), false);
            loginAttemptService.bloquearSiExcedeMaximo(command.username());

            if (loginAttemptService.estaEnLimite(command.username())) {
                auditService.registrar(TipoEvento.USUARIO_BLOQUEADO, usuario.getId(),
                        command.ip(), null, null);
            }

            throw new CredencialesInvalidasException(
                    loginAttemptService.intentosRestantes(command.username()));
        }

        // Login exitoso (SEQ_01 linea 73-92)
        loginAttemptService.registrarIntento(command.username(), command.ip(), true);
        loginAttemptService.limpiarIntentosFallidos(command.username());

        String accessToken = tokenService.emitirAccessToken(usuario.getId(), command.branchId());
        String refreshToken = tokenService.emitirRefreshToken(usuario.getId());

        auditService.registrar(TipoEvento.LOGIN_OK, usuario.getId(), command.ip(), null, null);

        return AuthResult.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // --- SEQ_03: Logout ---
    @Override
    public void logout(String jti, long expSeconds, String userId, String ip, String userAgent) {
        UUID uid = UUID.fromString(userId);
        tokenService.revocarToken(jti, expSeconds, EstadoToken.REVOCADO_LOGOUT);
        tokenService.revocarRefreshToken(null, uid);
        auditService.registrar(TipoEvento.LOGOUT, uid, ip, userAgent, null);
    }

    // --- SEQ_05: Logout por inactividad HIPAA ---
    @Override
    public void logoutByIdle(String jti, long expSeconds, String userId, String ip, String userAgent) {
        UUID uid = UUID.fromString(userId);
        tokenService.revocarToken(jti, expSeconds, EstadoToken.REVOCADO_IDLE);
        tokenService.revocarRefreshToken(null, uid);
        auditService.registrar(TipoEvento.IDLE_TIMEOUT, uid, ip, userAgent, "{\"minutos\": 15}");
    }

    // --- SEQ_04: Refresh token con rotacion ---
    @Override
    public AuthResult refresh(String refreshToken) {
        UUID userId = tokenService.validarRefreshToken(refreshToken);

        // Rotacion atomica (SEQ_04 linea 243-248)
        tokenService.revocarRefreshToken(refreshToken, userId);

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(UsuarioNoEncontradoException::new);

        String newAccessToken = tokenService.emitirAccessToken(userId, null);
        String newRefreshToken = tokenService.emitirRefreshToken(userId);

        return AuthResult.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    // --- SEQ_02: Validar token ---
    @Override
    public TokenClaims validarToken(String jwt) {
        return tokenService.validarToken(jwt);
    }
}
