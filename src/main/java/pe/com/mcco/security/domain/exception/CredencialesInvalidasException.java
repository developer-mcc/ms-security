package pe.com.mcco.security.domain.exception;

public class CredencialesInvalidasException extends DomainException {

    private final int intentosRestantes;

    public CredencialesInvalidasException(int intentosRestantes) {
        super("Credenciales invalidas");
        this.intentosRestantes = intentosRestantes;
    }

    public int getIntentosRestantes() {
        return intentosRestantes;
    }
}
