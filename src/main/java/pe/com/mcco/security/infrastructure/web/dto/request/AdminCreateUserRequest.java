package pe.com.mcco.security.infrastructure.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AdminCreateUserRequest(
        @NotBlank String username,
        @NotBlank String password,
        @Email String email,
        String celular,
        boolean activo
) {}
