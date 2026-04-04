package pe.com.mcco.security.application.dto;

import lombok.Builder;

@Builder
public record AuthResult(
        String accessToken,
        String refreshToken
) {}
