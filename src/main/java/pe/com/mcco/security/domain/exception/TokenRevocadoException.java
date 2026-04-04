package pe.com.mcco.security.domain.exception;

public class TokenRevocadoException extends DomainException {
    public TokenRevocadoException() {
        super("Token revocado");
    }
}
