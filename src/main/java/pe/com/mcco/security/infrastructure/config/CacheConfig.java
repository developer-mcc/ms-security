package pe.com.mcco.security.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.com.mcco.security.domain.port.out.TokenCachePort;
import pe.com.mcco.security.infrastructure.cache.postgres.PostgresTokenCacheAdapter;
import pe.com.mcco.security.infrastructure.cache.redis.RedisTokenCacheAdapter;
import pe.com.mcco.security.infrastructure.cache.repository.CacheEntryJpaRepository;

/**
 * Configuracion del cache provider.
 *
 * Para cambiar de PostgreSQL a Redis:
 * 1. En build.gradle: descomentar spring-boot-starter-data-redis
 * 2. En application.yaml: cambiar app.cache.provider a 'redis'
 * 3. Configurar spring.data.redis.host y port
 * 4. Completar la implementacion de RedisTokenCacheAdapter
 */
@Configuration
public class CacheConfig {

    @Bean
    @ConditionalOnProperty(name = "app.cache.provider", havingValue = "postgres", matchIfMissing = true)
    public TokenCachePort postgresTokenCache(CacheEntryJpaRepository cacheRepository) {
        return new PostgresTokenCacheAdapter(cacheRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "app.cache.provider", havingValue = "redis")
    public TokenCachePort redisTokenCache() {
        // TODO: inyectar RedisTemplate cuando la dependencia este habilitada
        return new RedisTokenCacheAdapter();
    }
}
