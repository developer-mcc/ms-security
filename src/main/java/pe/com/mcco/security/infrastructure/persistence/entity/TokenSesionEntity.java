package pe.com.mcco.security.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.com.mcco.security.domain.enums.EstadoToken;
import pe.com.mcco.security.domain.enums.TipoToken;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tokens_sesion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenSesionEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String jti;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoToken tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoToken estado;

    @Column(name = "branch_id")
    private String branchId;

    @Column(name = "emitido_en", nullable = false)
    private LocalDateTime emitidoEn;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    @Column(name = "ultimo_uso")
    private LocalDateTime ultimoUso;

    @Column(name = "revocado_en")
    private LocalDateTime revocadoEn;
}
