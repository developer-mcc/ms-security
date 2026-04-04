package pe.com.mcco.security.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rol {
    private UUID id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private boolean activo;
    private LocalDateTime creadoEn;
    private List<Permiso> permisos;
}
