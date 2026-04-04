package pe.com.mcco.security.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AsignarRolRequest(
        @NotBlank String codigoRol
) {}
