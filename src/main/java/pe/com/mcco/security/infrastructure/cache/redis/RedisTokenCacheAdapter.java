package pe.com.mcco.security.infrastructure.cache.redis;

import pe.com.mcco.security.domain.port.out.TokenCachePort;

import java.util.Optional;

/**
 * Implementacion de cache usando Redis.
 * Activado con app.cache.provider=redis en application.yaml.
 *
 * Para habilitar:
 * 1. Descomentar spring-boot-starter-data-redis en build.gradle
 * 2. Cambiar app.cache.provider=redis en application.yaml
 * 3. Configurar spring.data.redis.host y port
 * 4. Descomentar la inyeccion de RedisTemplate en esta clase
 */
public class RedisTokenCacheAdapter implements TokenCachePort {

    // TODO: descomentar cuando se agregue la dependencia de Redis
    // private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String BLOCKED_PREFIX = "blocked:";
    private static final String REFRESH_PREFIX = "refresh:";
    private static final String RESET_PREFIX = "reset:";

    // public RedisTokenCacheAdapter(RedisTemplate<String, String> redisTemplate) {
    //     this.redisTemplate = redisTemplate;
    // }

    @Override
    public void addToBlacklist(String jti, long ttlSeconds) {
        // redisTemplate.opsForValue().set(BLACKLIST_PREFIX + jti, "1", ttlSeconds, TimeUnit.SECONDS);
        throw new UnsupportedOperationException("Habilitar dependencia Redis en build.gradle");
    }

    @Override
    public boolean isBlacklisted(String jti) {
        // return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
        throw new UnsupportedOperationException("Habilitar dependencia Redis en build.gradle");
    }

    @Override
    public void bloquearUsuario(String username, long ttlSeconds) {
        // long epoch = Instant.now().plusSeconds(ttlSeconds).getEpochSecond();
        // redisTemplate.opsForValue().set(BLOCKED_PREFIX + username, String.valueOf(epoch), ttlSeconds, TimeUnit.SECONDS);
        throw new UnsupportedOperationException("Habilitar dependencia Redis en build.gradle");
    }

    @Override
    public Optional<Long> obtenerBloqueadoHastaEpoch(String username) {
        // String val = redisTemplate.opsForValue().get(BLOCKED_PREFIX + username);
        // return Optional.ofNullable(val).map(Long::parseLong);
        throw new UnsupportedOperationException("Habilitar dependencia Redis en build.gradle");
    }

    @Override
    public void desbloquearUsuario(String username) {
        // redisTemplate.delete(BLOCKED_PREFIX + username);
        throw new UnsupportedOperationException("Habilitar dependencia Redis en build.gradle");
    }

    @Override
    public void guardarRefreshToken(String userId, String token, long ttlSeconds) {
        // redisTemplate.opsForValue().set(REFRESH_PREFIX + userId, token, ttlSeconds, TimeUnit.SECONDS);
        throw new UnsupportedOperationException("Habilitar dependencia Redis en build.gradle");
    }

    @Override
    public Optional<String> obtenerRefreshToken(String userId) {
        // return Optional.ofNullable(redisTemplate.opsForValue().get(REFRESH_PREFIX + userId));
        throw new UnsupportedOperationException("Habilitar dependencia Redis en build.gradle");
    }

    @Override
    public void eliminarRefreshToken(String userId) {
        // redisTemplate.delete(REFRESH_PREFIX + userId);
        throw new UnsupportedOperationException("Habilitar dependencia Redis en build.gradle");
    }

    @Override
    public void guardarResetToken(String token, String userId, long ttlSeconds) {
        // redisTemplate.opsForValue().set(RESET_PREFIX + token, userId, ttlSeconds, TimeUnit.SECONDS);
        throw new UnsupportedOperationException("Habilitar dependencia Redis en build.gradle");
    }

    @Override
    public Optional<String> obtenerResetToken(String token) {
        // return Optional.ofNullable(redisTemplate.opsForValue().get(RESET_PREFIX + token));
        throw new UnsupportedOperationException("Habilitar dependencia Redis en build.gradle");
    }

    @Override
    public void eliminarResetToken(String token) {
        // redisTemplate.delete(RESET_PREFIX + token);
        throw new UnsupportedOperationException("Habilitar dependencia Redis en build.gradle");
    }
}
