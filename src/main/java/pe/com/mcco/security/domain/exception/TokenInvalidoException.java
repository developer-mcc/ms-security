package pe.com.mcco.security.domain.exception;

public class TokenInvalidoException extends DomainException {
    public TokenInvalidoException(String detalle) {
        super("Token invalido: " + detalle);
    }
}
