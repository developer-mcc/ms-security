package pe.com.mcco.security.infrastructure.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserCreatedResponse(
        UUID id,
        String username,
        String email,
        String mensaje
) {}
