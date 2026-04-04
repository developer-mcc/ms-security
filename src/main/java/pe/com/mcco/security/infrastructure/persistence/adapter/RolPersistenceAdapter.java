package pe.com.mcco.security.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcco.security.domain.model.Permiso;
import pe.com.mcco.security.domain.model.Rol;
import pe.com.mcco.security.domain.port.out.RolRepositoryPort;
import pe.com.mcco.security.infrastructure.persistence.entity.PermisoEntity;
import pe.com.mcco.security.infrastructure.persistence.entity.RolEntity;
import pe.com.mcco.security.infrastructure.persistence.repository.RolJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RolPersistenceAdapter implements RolRepositoryPort {

    private final RolJpaRepository jpa;

    @Override
    public Optional<Rol> findByCodigo(String codigo) {
        return jpa.findByCodigoAndActivo(codigo).map(this::toDomain);
    }

    @Override
    public List<Rol> findByCodigos(List<String> codigos) {
        return jpa.findByCodigoInAndActivo(codigos).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Rol> findByUsuarioId(UUID usuarioId) {
        return jpa.findByUsuarioId(usuarioId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Rol> findAllActivos() {
        return jpa.findAllActivos().stream().map(this::toDomain).toList();
    }

    @Override
    public void asignarRolAUsuario(UUID usuarioId, UUID rolId, UUID asignadoPor) {
        jpa.asignarRol(usuarioId, rolId, asignadoPor);
    }

    @Override
    public void quitarRolDeUsuario(UUID usuarioId, UUID rolId) {
        jpa.quitarRol(usuarioId, rolId);
    }

    private Rol toDomain(RolEntity e) {
        return Rol.builder()
                .id(e.getId())
                .codigo(e.getCodigo())
                .nombre(e.getNombre())
                .descripcion(e.getDescripcion())
                .activo(e.isActivo())
                .creadoEn(e.getFecCreacion())
                .permisos(e.getPermisos().stream().map(this::toPermisoDomain).toList())
                .build();
    }

    private Permiso toPermisoDomain(PermisoEntity e) {
        return Permiso.builder()
                .id(e.getId())
                .codigo(e.getCodigo())
                .nombre(e.getNombre())
                .modulo(e.getModulo())
                .build();
    }
}
