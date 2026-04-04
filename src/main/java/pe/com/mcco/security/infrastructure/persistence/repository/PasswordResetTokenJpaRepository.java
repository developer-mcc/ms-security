package pe.com.mcco.security.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcco.security.infrastructure.persistence.entity.PasswordResetTokenEntity;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    Optional<PasswordResetTokenEntity> findByTokenAndUsadoFalse(String token);

    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetTokenEntity t SET t.usado = true, t.usadoEn = CURRENT_TIMESTAMP WHERE t.id = :id")
    void marcarUsado(UUID id);
}
