package pe.com.mcco.security.domain.exception;

import java.util.List;

public class PasswordDebilException extends DomainException {

    private final List<String> reglas;

    public PasswordDebilException(List<String> reglasIncumplidas) {
        super("Password no cumple requisitos de seguridad");
        this.reglas = reglasIncumplidas;
    }

    public List<String> getReglas() {
        return reglas;
    }
}
