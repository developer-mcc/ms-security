package pe.com.mcco.security.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.com.mcco.security.domain.enums.TipoEvento;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEvento evento;

    @Column(name = "user_id")
    private UUID userId;

    private String ip;

    @Column(name = "user_agent")
    private String userAgent;

    private String detalle;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
