package pe.com.mcco.security.application.dto;

import lombok.Builder;

@Builder
public record RegisterUserCommand(
        String username,
        String password,
        String email,
        String celular
) {}
