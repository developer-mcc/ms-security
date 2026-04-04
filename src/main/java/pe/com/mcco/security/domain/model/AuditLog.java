package pe.com.mcco.security.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.com.mcco.security.domain.enums.TipoEvento;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    private UUID id;
    private TipoEvento evento;
    private UUID userId;
    private String ip;
    private String userAgent;
    private String detalle;
    private LocalDateTime creadoEn;
}
