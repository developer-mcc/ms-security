package pe.com.mcco.security.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pe.com.mcco.security.domain.enums.CanalNotificacion;

public record ForgotPasswordRequest(
        @NotBlank String identifier,
        @NotNull CanalNotificacion canal
) {}
