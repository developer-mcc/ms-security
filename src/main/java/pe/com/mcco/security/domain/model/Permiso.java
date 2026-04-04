package pe.com.mcco.security.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permiso {
    private UUID id;
    private String codigo;
    private String nombre;
    private String modulo;
}
