package pe.com.mcco.security.domain.exception;

public class UsuarioYaExisteException extends DomainException {
    public UsuarioYaExisteException(String campo) {
        super("Ya existe un usuario con ese " + campo);
    }
}
