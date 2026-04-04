package pe.com.mcco.security.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank String nuevaPassword
) {}
