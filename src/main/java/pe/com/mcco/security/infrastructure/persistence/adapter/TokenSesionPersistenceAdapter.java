package pe.com.mcco.security.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcco.security.domain.enums.EstadoToken;
import pe.com.mcco.security.domain.model.TokenSesion;
import pe.com.mcco.security.domain.port.out.TokenSesionRepositoryPort;
import pe.com.mcco.security.infrastructure.persistence.entity.TokenSesionEntity;
import pe.com.mcco.security.infrastructure.persistence.repository.TokenSesionJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TokenSesionPersistenceAdapter implements TokenSesionRepositoryPort {

    private final TokenSesionJpaRepository jpa;

    @Override
    public void save(TokenSesion token) {
        jpa.save(toEntity(token));
    }

    @Override
    public Optional<TokenSesion> findByJtiAndEstado(String jti, EstadoToken estado) {
        return jpa.findByJtiAndEstado(jti, estado).map(this::toDomain);
    }

    @Override
    public void updateEstado(String jti, EstadoToken estado) {
        jpa.updateEstado(jti, estado);
    }

    @Override
    public void updateUltimoUso(String jti) {
        jpa.updateUltimoUso(jti);
    }

    @Override
    public List<TokenSesion> findActivosByUserId(UUID userId) {
        return jpa.findByUserIdAndEstado(userId, EstadoToken.ACTIVO)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public void revocarTodosPorUsuario(UUID userId, EstadoToken estado) {
        jpa.revocarTodosPorUsuario(userId, estado);
    }

    private TokenSesion toDomain(TokenSesionEntity e) {
        return TokenSesion.builder()
                .id(e.getId())
                .jti(e.getJti())
                .userId(e.getUserId())
                .tipo(e.getTipo())
                .estado(e.getEstado())
                .branchId(e.getBranchId())
                .emitidoEn(e.getEmitidoEn())
                .expiraEn(e.getExpiraEn())
                .ultimoUso(e.getUltimoUso())
                .revocadoEn(e.getRevocadoEn())
                .build();
    }

    private TokenSesionEntity toEntity(TokenSesion t) {
        return TokenSesionEntity.builder()
                .id(t.getId())
                .jti(t.getJti())
                .userId(t.getUserId())
                .tipo(t.getTipo())
                .estado(t.getEstado())
                .branchId(t.getBranchId())
                .emitidoEn(t.getEmitidoEn())
                .expiraEn(t.getExpiraEn())
                .ultimoUso(t.getUltimoUso())
                .revocadoEn(t.getRevocadoEn())
                .build();
    }
}
