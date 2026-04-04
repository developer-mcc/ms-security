package pe.com.mcco.security.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    private UUID id;
    private String username;
    private String passwordHash;
    private String email;
    private String celular;
    private boolean activo;
    private LocalDateTime creadoEn;
    @Builder.Default
    private List<Rol> roles = new ArrayList<>();

    public List<String> getCodigosRoles() {
        return roles.stream().map(Rol::getCodigo).toList();
    }

    public List<String> getPermisos() {
        return roles.stream()
                .filter(Rol::isActivo)
                .flatMap(rol -> rol.getPermisos().stream())
                .map(Permiso::getCodigo)
                .distinct()
                .toList();
    }
}
