package pe.com.mcco.security.infrastructure.web.advice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.com.mcco.security.domain.exception.*;
import pe.com.mcco.security.infrastructure.web.dto.response.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // Métodos genéricos de trazado
    // -------------------------------------------------------------------------

    /**
     * Para excepciones de dominio esperadas (flujos normales de negocio).
     * Nivel WARN: no incluye stack trace porque no indican un fallo del sistema.
     */
    private ResponseEntity<ErrorResponse> handle(Exception ex, HttpStatus status,
                                                 ErrorResponse body, HttpServletRequest request) {
        String nombre = ex.getClass().getSimpleName();
        String uri = request.getRequestURI();

        log.warn("[INICIO] {} | uri={} | causa={}", nombre, uri, ex.getMessage());
        ResponseEntity<ErrorResponse> response = ResponseEntity.status(status).body(body);
        log.warn("[FIN]    {} | uri={} | httpStatus={}", nombre, uri, status.value());

        return response;
    }

    /**
     * Para excepciones inesperadas (bugs, errores de infraestructura).
     * Nivel ERROR: incluye stack trace completo para diagnóstico.
     */
    private ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpStatus status,
                                                           ErrorResponse body, HttpServletRequest request) {
        String nombre = ex.getClass().getSimpleName();
        String uri = request.getRequestURI();

        log.error("[INICIO] {} | uri={} | causa={}", nombre, uri, ex.getMessage(), ex);
        ResponseEntity<ErrorResponse> response = ResponseEntity.status(status).body(body);
        log.error("[FIN]    {} | uri={} | httpStatus={}", nombre, uri, status.value());

        return response;
    }

    // -------------------------------------------------------------------------
    // Seguridad — autenticación / sesión
    // -------------------------------------------------------------------------

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> handleCredenciales(CredencialesInvalidasException ex,
                                                            HttpServletRequest request) {
        return handle(ex, HttpStatus.UNAUTHORIZED,
                ErrorResponse.builder()
                        .mensaje("Credenciales invalidas")
                        .intentosRestantes(ex.getIntentosRestantes())
                        .build(),
                request);
    }

    @ExceptionHandler(UsuarioBloqueadoException.class)
    public ResponseEntity<ErrorResponse> handleBloqueado(UsuarioBloqueadoException ex,
                                                         HttpServletRequest request) {
        return handle(ex, HttpStatus.valueOf(423),
                ErrorResponse.builder()
                        .mensaje("Usuario bloqueado")
                        .bloqueadoHasta(ex.getBloqueadoHasta().toString())
                        .build(),
                request);
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex,
                                                                   HttpServletRequest request) {
        // OWASP: el log interno registra la causa real; la respuesta no revela si el usuario existe
        return handle(ex, HttpStatus.UNAUTHORIZED,
                ErrorResponse.builder()
                        .mensaje("Credenciales invalidas")
                        .build(),
                request);
    }

    @ExceptionHandler(UsuarioYaExisteException.class)
    public ResponseEntity<ErrorResponse> handleUsuarioYaExiste(UsuarioYaExisteException ex,
                                                               HttpServletRequest request) {
        return handle(ex, HttpStatus.CONFLICT,
                ErrorResponse.builder()
                        .mensaje(ex.getMessage())
                        .build(),
                request);
    }

    // -------------------------------------------------------------------------
    // Seguridad — tokens
    // -------------------------------------------------------------------------

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleTokenInvalido(TokenInvalidoException ex,
                                                             HttpServletRequest request) {
        return handle(ex, HttpStatus.UNAUTHORIZED,
                ErrorResponse.builder()
                        .mensaje(ex.getMessage())
                        .build(),
                request);
    }

    @ExceptionHandler(TokenRevocadoException.class)
    public ResponseEntity<ErrorResponse> handleTokenRevocado(TokenRevocadoException ex,
                                                             HttpServletRequest request) {
        return handle(ex, HttpStatus.UNAUTHORIZED,
                ErrorResponse.builder()
                        .mensaje("TOKEN_REVOCADO")
                        .build(),
                request);
    }

    @ExceptionHandler(TokenExpiradoException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpirado(TokenExpiradoException ex,
                                                             HttpServletRequest request) {
        return handle(ex, HttpStatus.UNAUTHORIZED,
                ErrorResponse.builder()
                        .mensaje("TOKEN_EXPIRADO")
                        .build(),
                request);
    }

    @ExceptionHandler(RefreshTokenInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleRefreshInvalido(RefreshTokenInvalidoException ex,
                                                               HttpServletRequest request) {
        return handle(ex, HttpStatus.UNAUTHORIZED,
                ErrorResponse.builder()
                        .mensaje("SESSION_EXPIRED")
                        .build(),
                request);
    }

    // -------------------------------------------------------------------------
    // Reglas de negocio — contraseñas
    // -------------------------------------------------------------------------

    @ExceptionHandler(PasswordDebilException.class)
    public ResponseEntity<ErrorResponse> handlePasswordDebil(PasswordDebilException ex,
                                                             HttpServletRequest request) {
        return handle(ex, HttpStatus.BAD_REQUEST,
                ErrorResponse.builder()
                        .mensaje("Password no cumple requisitos de seguridad")
                        .errores(ex.getReglas())
                        .build(),
                request);
    }

    @ExceptionHandler(PasswordRepetidoException.class)
    public ResponseEntity<ErrorResponse> handlePasswordRepetido(PasswordRepetidoException ex,
                                                                HttpServletRequest request) {
        return handle(ex, HttpStatus.BAD_REQUEST,
                ErrorResponse.builder()
                        .mensaje(ex.getMessage())
                        .build(),
                request);
    }

    // -------------------------------------------------------------------------
    // Validación de entrada
    // -------------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        var errores = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return handle(ex, HttpStatus.BAD_REQUEST,
                ErrorResponse.builder()
                        .mensaje("Error de validacion")
                        .errores(errores)
                        .build(),
                request);
    }

    // -------------------------------------------------------------------------
    // Fallback — errores no contemplados
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return handleUnexpected(ex, HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorResponse.builder()
                        .mensaje("Error interno del servidor")
                        .build(),
                request);
    }
}
