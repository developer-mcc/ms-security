package pe.com.mcco.security.domain.port.out;

import java.util.Optional;

/**
 * Puerto de cache para tokens. Implementado por PostgreSQL (default) o Redis (cuando haya presupuesto).
 * Cambiar app.cache.provider=redis en application.yaml para activar Redis.
 */
public interface TokenCachePort {

    // --- Blacklist de access tokens (jti revocados) ---
    void addToBlacklist(String jti, long ttlSeconds);

    boolean isBlacklisted(String jti);

    // --- Bloqueo de usuarios por intentos fallidos ---
    void bloquearUsuario(String username, long ttlSeconds);

    Optional<Long> obtenerBloqueadoHastaEpoch(String username);

    void desbloquearUsuario(String username);

    // --- Refresh tokens ---
    void guardarRefreshToken(String userId, String token, long ttlSeconds);

    Optional<String> obtenerRefreshToken(String userId);

    void eliminarRefreshToken(String userId);

    // --- Reset password tokens ---
    void guardarResetToken(String token, String userId, long ttlSeconds);

    Optional<String> obtenerResetToken(String token);

    void eliminarResetToken(String token);
}
