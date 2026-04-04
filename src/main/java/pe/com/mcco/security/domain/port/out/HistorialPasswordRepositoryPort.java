package pe.com.mcco.security.domain.port.out;

import pe.com.mcco.security.domain.model.HistorialPassword;

import java.util.List;
import java.util.UUID;

public interface HistorialPasswordRepositoryPort {

    void save(HistorialPassword historial);

    List<HistorialPassword> findRecientesByUserId(UUID userId, int limite);
}
