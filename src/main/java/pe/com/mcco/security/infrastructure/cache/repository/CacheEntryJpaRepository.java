package pe.com.mcco.security.infrastructure.cache.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcco.security.infrastructure.cache.entity.CacheEntryEntity;

import java.util.Optional;

public interface CacheEntryJpaRepository extends JpaRepository<CacheEntryEntity, String> {

    @Query("SELECT c FROM CacheEntryEntity c WHERE c.cacheKey = :key AND c.expiraEn > CURRENT_TIMESTAMP")
    Optional<CacheEntryEntity> findByKeyNotExpired(String key);

    @Modifying
    @Transactional
    @Query("DELETE FROM CacheEntryEntity c WHERE c.expiraEn <= CURRENT_TIMESTAMP")
    void limpiarExpirados();
}
