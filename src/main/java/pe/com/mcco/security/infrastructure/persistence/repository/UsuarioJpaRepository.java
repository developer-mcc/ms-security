package pe.com.mcco.security.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcco.security.infrastructure.persistence.entity.UsuarioEntity;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {

    @Query("SELECT u FROM UsuarioEntity u WHERE u.username = :username AND u.estado = 'A'")
    Optional<UsuarioEntity> findByUsernameAndActivo(String username);

    @Query("SELECT u FROM UsuarioEntity u WHERE u.email = :identifier OR u.celular = :identifier")
    Optional<UsuarioEntity> findByEmailOrCelular(String identifier);

    @Modifying
    @Transactional
    @Query("UPDATE UsuarioEntity u SET u.passwordHash = :hash WHERE u.id = :id")
    void updatePasswordHash(UUID id, String hash);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
