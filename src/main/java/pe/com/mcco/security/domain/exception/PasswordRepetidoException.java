package pe.com.mcco.security.domain.exception;

public class PasswordRepetidoException extends DomainException {
    public PasswordRepetidoException() {
        super("Password ya fue usado recientemente");
    }
}
