package pe.com.mcco.security.domain.exception;

import java.time.LocalDateTime;

public class UsuarioBloqueadoException extends DomainException {

    private final LocalDateTime bloqueadoHasta;

    public UsuarioBloqueadoException(LocalDateTime bloqueadoHasta) {
        super("Usuario bloqueado hasta " + bloqueadoHasta);
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }
}
