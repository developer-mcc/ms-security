package pe.com.mcco.security.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "login_intentos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginIntentoEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String username;

    private String ip;

    @Column(nullable = false)
    private boolean exitoso;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
