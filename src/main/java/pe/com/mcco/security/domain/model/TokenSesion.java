package pe.com.mcco.security.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.com.mcco.security.domain.enums.EstadoToken;
import pe.com.mcco.security.domain.enums.TipoToken;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenSesion {
    private UUID id;
    private String jti;
    private UUID userId;
    private TipoToken tipo;
    private EstadoToken estado;
    private String branchId;
    private LocalDateTime emitidoEn;
    private LocalDateTime expiraEn;
    private LocalDateTime ultimoUso;
    private LocalDateTime revocadoEn;
}
