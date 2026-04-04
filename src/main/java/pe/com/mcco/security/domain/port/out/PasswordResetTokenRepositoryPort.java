package pe.com.mcco.security.domain.port.out;

import pe.com.mcco.security.domain.model.PasswordResetToken;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepositoryPort {

    void save(PasswordResetToken token);

    Optional<PasswordResetToken> findByTokenAndNoUsado(String token);

    void marcarUsado(UUID id);
}
