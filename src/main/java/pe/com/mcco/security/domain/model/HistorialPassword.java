package pe.com.mcco.security.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialPassword {
    private UUID id;
    private UUID userId;
    private String passwordHash;
    private LocalDateTime cambiadoEn;
}
