package pe.com.mcco.security.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcco.security.domain.enums.EstadoToken;
import pe.com.mcco.security.infrastructure.persistence.entity.TokenSesionEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenSesionJpaRepository extends JpaRepository<TokenSesionEntity, UUID> {

    Optional<TokenSesionEntity> findByJtiAndEstado(String jti, EstadoToken estado);

    List<TokenSesionEntity> findByUserIdAndEstado(UUID userId, EstadoToken estado);

    @Modifying
    @Transactional
    @Query("UPDATE TokenSesionEntity t SET t.estado = :estado, t.revocadoEn = CURRENT_TIMESTAMP WHERE t.jti = :jti")
    void updateEstado(String jti, EstadoToken estado);

    @Modifying
    @Transactional
    @Query("UPDATE TokenSesionEntity t SET t.ultimoUso = CURRENT_TIMESTAMP WHERE t.jti = :jti")
    void updateUltimoUso(String jti);

    @Modifying
    @Transactional
    @Query("UPDATE TokenSesionEntity t SET t.estado = :estado, t.revocadoEn = CURRENT_TIMESTAMP " +
           "WHERE t.userId = :userId AND t.estado = 'ACTIVO'")
    void revocarTodosPorUsuario(UUID userId, EstadoToken estado);
}
