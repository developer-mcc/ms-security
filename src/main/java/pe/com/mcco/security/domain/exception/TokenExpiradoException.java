package pe.com.mcco.security.domain.exception;

public class TokenExpiradoException extends DomainException {
    public TokenExpiradoException() {
        super("Token expirado");
    }
}
