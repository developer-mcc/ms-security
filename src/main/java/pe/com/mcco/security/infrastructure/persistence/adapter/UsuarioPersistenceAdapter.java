package pe.com.mcco.security.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcco.security.domain.model.Permiso;
import pe.com.mcco.security.domain.model.Rol;
import pe.com.mcco.security.domain.model.Usuario;
import pe.com.mcco.security.domain.port.out.UsuarioRepositoryPort;
import pe.com.mcco.security.infrastructure.persistence.entity.AuditableEntity;
import pe.com.mcco.security.infrastructure.persistence.entity.PermisoEntity;
import pe.com.mcco.security.infrastructure.persistence.entity.RolEntity;
import pe.com.mcco.security.infrastructure.persistence.entity.UsuarioEntity;
import pe.com.mcco.security.infrastructure.persistence.repository.UsuarioJpaRepository;

import java.util.Collections;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository jpa;

    @Override
    public Optional<Usuario> findByUsernameAndActivo(String username) {
        return jpa.findByUsernameAndActivo(username).map(this::toDomain);
    }

    @Override
    public Optional<Usuario> findByEmailOrCelular(String identifier) {
        return jpa.findByEmailOrCelular(identifier).map(this::toDomain);
    }

    @Override
    public Optional<Usuario> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public void updatePasswordHash(UUID userId, String newHash) {
        jpa.updatePasswordHash(userId, newHash);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpa.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioEntity saved = jpa.save(toEntity(usuario));
        return toDomain(saved);
    }

    private UsuarioEntity toEntity(Usuario u) {
        UsuarioEntity entity = UsuarioEntity.builder()
                .id(u.getId())
                .username(u.getUsername())
                .passwordHash(u.getPasswordHash())
                .email(u.getEmail())
                .celular(u.getCelular())
                .estado(u.isActivo() ? AuditableEntity.ACTIVO : AuditableEntity.INACTIVO)
                .build();
        return entity;
    }

    private Usuario toDomain(UsuarioEntity e) {
        return Usuario.builder()
                .id(e.getId())
                .username(e.getUsername())
                .passwordHash(e.getPasswordHash())
                .email(e.getEmail())
                .celular(e.getCelular())
                .activo(e.isActivo())
                .creadoEn(e.getFecCreacion())
                .roles(e.getRoles() != null ? e.getRoles().stream().map(this::toRolDomain).toList() : Collections.emptyList())
                .build();
    }

    private Rol toRolDomain(RolEntity r) {
        return Rol.builder()
                .id(r.getId())
                .codigo(r.getCodigo())
                .nombre(r.getNombre())
                .activo(r.isActivo())
                .permisos(r.getPermisos() != null ? r.getPermisos().stream().map(this::toPermisoDomain).toList() : Collections.emptyList())
                .build();
    }

    private Permiso toPermisoDomain(PermisoEntity p) {
        return Permiso.builder()
                .id(p.getId())
                .codigo(p.getCodigo())
                .nombre(p.getNombre())
                .modulo(p.getModulo())
                .build();
    }
}
