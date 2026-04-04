package pe.com.mcco.security.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcco.security.infrastructure.persistence.entity.LoginIntentoEntity;

import java.util.UUID;

public interface LoginIntentoJpaRepository extends JpaRepository<LoginIntentoEntity, UUID> {

    @Query("SELECT COUNT(l) FROM LoginIntentoEntity l WHERE l.username = :username AND l.exitoso = false")
    int contarFallidosRecientes(String username);

    @Modifying
    @Transactional
    @Query("DELETE FROM LoginIntentoEntity l WHERE l.username = :username AND l.exitoso = false")
    void eliminarFallidosPrevios(String username);
}
