package pe.com.mcco.security.infrastructure.config;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pe.com.mcco.security.application.dto.TokenClaims;

import java.util.Optional;

/**
 * Resuelve el usuario actual desde el SecurityContext para las columnas
 * usr_creacion y usr_modificacion.
 * <p>
 * Si no hay usuario autenticado (ej: registro, login), usa "SYSTEM".
 */
@NullMarked
public class AuditorAwareImpl implements AuditorAware<String> {

    private static final String SYSTEM_USER = "SYSTEM";

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of(SYSTEM_USER);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof TokenClaims claims) {
            return Optional.of(claims.userId());
        }

        return Optional.of(SYSTEM_USER);
    }
}
