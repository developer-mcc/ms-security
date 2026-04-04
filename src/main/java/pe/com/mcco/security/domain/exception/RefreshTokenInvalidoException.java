package pe.com.mcco.security.domain.exception;

public class RefreshTokenInvalidoException extends DomainException {
    public RefreshTokenInvalidoException() {
        super("Refresh token invalido o expirado");
    }
}
