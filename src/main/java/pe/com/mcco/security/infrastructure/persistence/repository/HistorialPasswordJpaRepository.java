package pe.com.mcco.security.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.com.mcco.security.infrastructure.persistence.entity.HistorialPasswordEntity;

import java.util.List;
import java.util.UUID;

public interface HistorialPasswordJpaRepository extends JpaRepository<HistorialPasswordEntity, UUID> {

    @Query("SELECT h FROM HistorialPasswordEntity h WHERE h.userId = :userId ORDER BY h.cambiadoEn DESC LIMIT :limite")
    List<HistorialPasswordEntity> findRecientesByUserId(UUID userId, int limite);
}
