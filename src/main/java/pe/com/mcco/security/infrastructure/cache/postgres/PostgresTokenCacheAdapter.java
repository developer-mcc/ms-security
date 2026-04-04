package pe.com.mcco.security.infrastructure.cache.postgres;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcco.security.domain.port.out.TokenCachePort;
import pe.com.mcco.security.infrastructure.cache.entity.CacheEntryEntity;
import pe.com.mcco.security.infrastructure.cache.repository.CacheEntryJpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementacion de cache usando PostgreSQL como key-value store con TTL.
 * Activado por defecto con app.cache.provider=postgres.
 * Cuando haya presupuesto para Redis, cambiar a app.cache.provider=redis.
 */
@RequiredArgsConstructor
@Transactional
public class PostgresTokenCacheAdapter implements TokenCachePort {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String BLOCKED_PREFIX = "blocked:";
    private static final String REFRESH_PREFIX = "refresh:";
    private static final String RESET_PREFIX = "reset:";

    private final CacheEntryJpaRepository cacheRepository;

    // --- Blacklist ---

    @Override
    public void addToBlacklist(String jti, long ttlSeconds) {
        upsert(BLACKLIST_PREFIX + jti, "1", ttlSeconds);
    }

    @Override
    public boolean isBlacklisted(String jti) {
        return cacheRepository.findByKeyNotExpired(BLACKLIST_PREFIX + jti).isPresent();
    }

    // --- Bloqueo de usuarios ---

    @Override
    public void bloquearUsuario(String username, long ttlSeconds) {
        long epoch = java.time.Instant.now().plusSeconds(ttlSeconds).getEpochSecond();
        upsert(BLOCKED_PREFIX + username, String.valueOf(epoch), ttlSeconds);
    }

    @Override
    public Optional<Long> obtenerBloqueadoHastaEpoch(String username) {
        return cacheRepository.findByKeyNotExpired(BLOCKED_PREFIX + username)
                .map(e -> Long.parseLong(e.getCacheValue()));
    }

    @Override
    public void desbloquearUsuario(String username) {
        cacheRepository.deleteById(BLOCKED_PREFIX + username);
    }

    // --- Refresh tokens ---

    @Override
    public void guardarRefreshToken(String userId, String token, long ttlSeconds) {
        upsert(REFRESH_PREFIX + userId, token, ttlSeconds);
    }

    @Override
    public Optional<String> obtenerRefreshToken(String userId) {
        return cacheRepository.findByKeyNotExpired(REFRESH_PREFIX + userId)
                .map(CacheEntryEntity::getCacheValue);
    }

    @Override
    public void eliminarRefreshToken(String userId) {
        cacheRepository.deleteById(REFRESH_PREFIX + userId);
    }

    // --- Reset tokens ---

    @Override
    public void guardarResetToken(String token, String userId, long ttlSeconds) {
        upsert(RESET_PREFIX + token, userId, ttlSeconds);
    }

    @Override
    public Optional<String> obtenerResetToken(String token) {
        return cacheRepository.findByKeyNotExpired(RESET_PREFIX + token)
                .map(CacheEntryEntity::getCacheValue);
    }

    @Override
    public void eliminarResetToken(String token) {
        cacheRepository.deleteById(RESET_PREFIX + token);
    }

    // --- Helper ---

    private void upsert(String key, String value, long ttlSeconds) {
        CacheEntryEntity entry = CacheEntryEntity.builder()
                .cacheKey(key)
                .cacheValue(value)
                .expiraEn(LocalDateTime.now().plusSeconds(ttlSeconds))
                .build();
        cacheRepository.save(entry);
    }
}
