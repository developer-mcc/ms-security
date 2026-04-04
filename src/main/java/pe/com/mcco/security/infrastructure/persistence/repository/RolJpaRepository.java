package pe.com.mcco.security.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcco.security.infrastructure.persistence.entity.RolEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RolJpaRepository extends JpaRepository<RolEntity, UUID> {

    @Query("SELECT r FROM RolEntity r WHERE r.codigo = :codigo AND r.estado = 'A'")
    Optional<RolEntity> findByCodigoAndActivo(String codigo);

    @Query("SELECT r FROM RolEntity r WHERE r.codigo IN :codigos AND r.estado = 'A'")
    List<RolEntity> findByCodigoInAndActivo(List<String> codigos);

    @Query("SELECT r FROM RolEntity r WHERE r.estado = 'A'")
    List<RolEntity> findAllActivos();

    @Query("SELECT r FROM RolEntity r JOIN r.permisos p " +
           "WHERE r IN (SELECT ur FROM UsuarioEntity u JOIN u.roles ur WHERE u.id = :usuarioId) " +
           "AND r.estado = 'A'")
    List<RolEntity> findByUsuarioId(UUID usuarioId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO usuario_roles (usuario_id, rol_id, asignado_en, asignado_por) " +
                   "VALUES (:usuarioId, :rolId, CURRENT_TIMESTAMP, :asignadoPor)", nativeQuery = true)
    void asignarRol(UUID usuarioId, UUID rolId, UUID asignadoPor);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM usuario_roles WHERE usuario_id = :usuarioId AND rol_id = :rolId", nativeQuery = true)
    void quitarRol(UUID usuarioId, UUID rolId);
}
