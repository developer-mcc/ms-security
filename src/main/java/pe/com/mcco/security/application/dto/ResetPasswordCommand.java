package pe.com.mcco.security.application.dto;

import lombok.Builder;

@Builder
public record ResetPasswordCommand(
        String token,
        String nuevaPassword
) {}
