package pe.com.mcco.security.infrastructure.cache.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Tabla que simula un key-value store con TTL, reemplazando Redis
 * cuando app.cache.provider=postgres (default).
 */
@Entity
@Table(name = "cache_entries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheEntryEntity {

    @Id
    @Column(name = "cache_key", length = 255)
    private String cacheKey;

    @Column(name = "cache_value", nullable = false)
    private String cacheValue;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;
}
