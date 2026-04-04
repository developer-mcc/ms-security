package pe.com.mcco.security.domain.port.out;

import pe.com.mcco.security.domain.enums.EstadoToken;
import pe.com.mcco.security.domain.model.TokenSesion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenSesionRepositoryPort {

    void save(TokenSesion token);

    Optional<TokenSesion> findByJtiAndEstado(String jti, EstadoToken estado);

    void updateEstado(String jti, EstadoToken estado);

    void updateUltimoUso(String jti);

    List<TokenSesion> findActivosByUserId(UUID userId);

    void revocarTodosPorUsuario(UUID userId, EstadoToken estado);
}
