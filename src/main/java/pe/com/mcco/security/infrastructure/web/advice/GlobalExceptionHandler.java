package pe.com.mcco.security.infrastructure.web.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.com.mcco.security.domain.exception.*;
import pe.com.mcco.security.domain.exception.UsuarioYaExisteException;
import pe.com.mcco.security.infrastructure.web.dto.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> handleCredenciales(CredencialesInvalidasException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .mensaje("Credenciales invalidas")
                        .intentosRestantes(e.getIntentosRestantes())
                        .build());
    }

    @ExceptionHandler(UsuarioBloqueadoException.class)
    public ResponseEntity<ErrorResponse> handleBloqueado(UsuarioBloqueadoException e) {
        return ResponseEntity.status(423) // 423 Locked
                .body(ErrorResponse.builder()
                        .mensaje("Usuario bloqueado")
                        .bloqueadoHasta(e.getBloqueadoHasta().toString())
                        .build());
    }

    @ExceptionHandler(PasswordDebilException.class)
    public ResponseEntity<ErrorResponse> handlePasswordDebil(PasswordDebilException e) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .mensaje("Password no cumple requisitos de seguridad")
                        .errores(e.getReglas())
                        .build());
    }

    @ExceptionHandler(PasswordRepetidoException.class)
    public ResponseEntity<ErrorResponse> handlePasswordRepetido(PasswordRepetidoException e) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .mensaje(e.getMessage())
                        .build());
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleTokenInvalido(TokenInvalidoException e) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .mensaje(e.getMessage())
                        .build());
    }

    @ExceptionHandler(RefreshTokenInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleRefreshInvalido(RefreshTokenInvalidoException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .mensaje("SESSION_EXPIRED")
                        .build());
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleUsuarioNoEncontrado(UsuarioNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .mensaje("Credenciales invalidas")
                        .build());
    }

    @ExceptionHandler(UsuarioYaExisteException.class)
    public ResponseEntity<ErrorResponse> handleUsuarioYaExiste(UsuarioYaExisteException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .mensaje(e.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        var errores = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .mensaje("Error de validacion")
                        .errores(errores)
                        .build());
    }
}
