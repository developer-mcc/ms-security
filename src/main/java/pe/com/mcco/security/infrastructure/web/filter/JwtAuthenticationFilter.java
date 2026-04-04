package pe.com.mcco.security.infrastructure.web.filter;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.com.mcco.security.application.dto.TokenClaims;
import pe.com.mcco.security.application.service.AuditApplicationService;
import pe.com.mcco.security.application.service.TokenApplicationService;
import pe.com.mcco.security.domain.enums.TipoEvento;
import pe.com.mcco.security.domain.exception.TokenExpiradoException;
import pe.com.mcco.security.domain.exception.TokenInvalidoException;
import pe.com.mcco.security.domain.exception.TokenRevocadoException;
import pe.com.mcco.security.infrastructure.web.dto.response.ErrorResponse;

import java.io.IOException;
import java.util.UUID;

/**
 * SEQ_02: Validacion de token en cada request.
 * Extrae JWT del header Authorization, valida firma RS256,
 * verifica blacklist y estado en BD.
 * Propaga headers X-Auth-UserId, X-Auth-BranchId, X-Auth-Roles al BFF.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenApplicationService tokenService;
    private final AuditApplicationService auditService;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/login")
                || path.startsWith("/auth/register")
                || path.startsWith("/auth/forgot-password")
                || path.startsWith("/auth/reset-password")
                || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, HttpStatus.UNAUTHORIZED, "Token requerido");
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            TokenClaims claims = tokenService.validarToken(jwt);

            // Setear SecurityContext con ROLE_ + permisos granulares
            var authorities = new java.util.ArrayList<SimpleGrantedAuthority>();

            // ROLE_ADMIN, ROLE_CAJERO, etc. -> para hasRole("ADMIN")
            claims.roles().forEach(role ->
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));

            // USUARIOS_CREAR, VENTAS_CREAR, etc. -> para hasAuthority("USUARIOS_CREAR")
            claims.permisos().forEach(permiso ->
                    authorities.add(new SimpleGrantedAuthority(permiso)));

            var authentication = new UsernamePasswordAuthenticationToken(claims, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Headers para el BFF (SEQ_02 linea 161)
            request.setAttribute("X-Auth-UserId", claims.userId());
            request.setAttribute("X-Auth-BranchId", claims.branchId());
            request.setAttribute("X-Auth-Roles", String.join(",", claims.roles()));
            request.setAttribute("X-Auth-Permisos", String.join(",", claims.permisos()));

            chain.doFilter(request, response);

        } catch (TokenInvalidoException e) {
            auditService.registrar(TipoEvento.TOKEN_FIRMA_INVALIDA, null,
                    request.getRemoteAddr(), request.getHeader("User-Agent"), null);
            sendError(response, HttpStatus.UNAUTHORIZED, "TOKEN_INVALID");

        } catch (TokenRevocadoException e) {
            auditService.registrar(TipoEvento.TOKEN_REVOCADO_INTENTO, null,
                    request.getRemoteAddr(), request.getHeader("User-Agent"), null);
            sendError(response, HttpStatus.UNAUTHORIZED, "TOKEN_REVOKED");

        } catch (TokenExpiradoException e) {
            sendError(response, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED");
        }
    }

    private void sendError(HttpServletResponse response, HttpStatus status, String mensaje) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse error = ErrorResponse.builder()
                .mensaje(mensaje)
                .build();

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
