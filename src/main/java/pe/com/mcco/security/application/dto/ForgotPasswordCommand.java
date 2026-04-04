package pe.com.mcco.security.application.dto;

import lombok.Builder;
import pe.com.mcco.security.domain.enums.CanalNotificacion;

@Builder
public record ForgotPasswordCommand(
        String identifier,
        CanalNotificacion canal
) {}
