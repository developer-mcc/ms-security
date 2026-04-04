package pe.com.mcco.security.infrastructure.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String mensaje,
        List<String> errores,
        String bloqueadoHasta,
        Integer intentosRestantes
) {}
