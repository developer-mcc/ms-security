package pe.com.mcco.security.infrastructure.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String accessToken,
        String mensaje,
        Integer intentosRestantes,
        String bloqueadoHasta
) {}
