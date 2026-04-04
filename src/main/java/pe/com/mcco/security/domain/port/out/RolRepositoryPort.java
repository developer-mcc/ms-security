package pe.com.mcco.security.domain.port.out;

import pe.com.mcco.security.domain.model.Rol;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RolRepositoryPort {

    Optional<Rol> findByCodigo(String codigo);

    List<Rol> findByCodigos(List<String> codigos);

    List<Rol> findByUsuarioId(UUID usuarioId);

    List<Rol> findAllActivos();

    void asignarRolAUsuario(UUID usuarioId, UUID rolId, UUID asignadoPor);

    void quitarRolDeUsuario(UUID usuarioId, UUID rolId);
}
