package pe.com.mcco.security.application.dto;

import lombok.Builder;

@Builder
public record LoginCommand(
        String username,
        String password,
        String branchId,
        String ip
) {}
