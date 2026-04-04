package pe.com.mcco.security.domain.exception;

public class UsuarioNoEncontradoException extends DomainException {
    public UsuarioNoEncontradoException() {
        super("Usuario no encontrado");
    }
}
