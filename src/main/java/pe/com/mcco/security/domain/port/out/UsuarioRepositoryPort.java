package pe.com.mcco.security.domain.port.out;

import pe.com.mcco.security.domain.model.Usuario;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepositoryPort {

    Optional<Usuario> findByUsernameAndActivo(String username);

    Optional<Usuario> findByEmailOrCelular(String identifier);

    Optional<Usuario> findById(UUID id);

    void updatePasswordHash(UUID userId, String newHash);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Usuario save(Usuario usuario);
}
