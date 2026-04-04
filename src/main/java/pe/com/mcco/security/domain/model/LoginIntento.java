package pe.com.mcco.security.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginIntento {
    private UUID id;
    private String username;
    private String ip;
    private boolean exitoso;
    private LocalDateTime creadoEn;
}
