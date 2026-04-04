package pe.com.mcco.security.infrastructure.web.handler;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import pe.com.mcco.security.infrastructure.web.dto.response.ErrorResponse;

import java.io.IOException;

/**
 * Maneja requests no autenticadas que Spring Security rechaza.
 * Garantiza respuesta JSON uniforme con 401 en lugar del comportamiento
 * por defecto (HTML o body vacio).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.debug("Acceso no autenticado a {}: {}", request.getRequestURI(), authException.getMessage());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse error = ErrorResponse.builder()
                .mensaje("Autenticacion requerida")
                .build();

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
