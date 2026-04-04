package pe.com.mcco.security.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcco.security.domain.model.PasswordResetToken;
import pe.com.mcco.security.domain.port.out.PasswordResetTokenRepositoryPort;
import pe.com.mcco.security.infrastructure.persistence.entity.PasswordResetTokenEntity;
import pe.com.mcco.security.infrastructure.persistence.repository.PasswordResetTokenJpaRepository;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenPersistenceAdapter implements PasswordResetTokenRepositoryPort {

    private final PasswordResetTokenJpaRepository jpa;

    @Override
    public void save(PasswordResetToken token) {
        jpa.save(PasswordResetTokenEntity.builder()
                .id(token.getId())
                .userId(token.getUserId())
                .token(token.getToken())
                .canal(token.getCanal())
                .expiraEn(token.getExpiraEn())
                .usado(token.isUsado())
                .usadoEn(token.getUsadoEn())
                .build());
    }

    @Override
    public Optional<PasswordResetToken> findByTokenAndNoUsado(String token) {
        return jpa.findByTokenAndUsadoFalse(token).map(this::toDomain);
    }

    @Override
    public void marcarUsado(UUID id) {
        jpa.marcarUsado(id);
    }

    private PasswordResetToken toDomain(PasswordResetTokenEntity e) {
        return PasswordResetToken.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .token(e.getToken())
                .canal(e.getCanal())
                .expiraEn(e.getExpiraEn())
                .usado(e.isUsado())
                .usadoEn(e.getUsadoEn())
                .build();
    }
}
