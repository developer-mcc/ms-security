package pe.com.mcco.security.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.com.mcco.security.domain.enums.CanalNotificacion;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
    private UUID id;
    private UUID userId;
    private String token;
    private CanalNotificacion canal;
    private LocalDateTime expiraEn;
    private boolean usado;
    private LocalDateTime usadoEn;
}
