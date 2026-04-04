package pe.com.mcco.security.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record TokenClaims(
        String userId,
        String branchId,
        List<String> roles,
        List<String> permisos,
        String jti
) {}
