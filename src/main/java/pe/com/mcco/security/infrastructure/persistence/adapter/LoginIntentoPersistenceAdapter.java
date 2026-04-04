package pe.com.mcco.security.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcco.security.domain.model.LoginIntento;
import pe.com.mcco.security.domain.port.out.LoginIntentoRepositoryPort;
import pe.com.mcco.security.infrastructure.persistence.entity.LoginIntentoEntity;
import pe.com.mcco.security.infrastructure.persistence.repository.LoginIntentoJpaRepository;

@Component
@RequiredArgsConstructor
public class LoginIntentoPersistenceAdapter implements LoginIntentoRepositoryPort {

    private final LoginIntentoJpaRepository jpa;
    private final EntityManager entityManager;

    @Override
    public void save(LoginIntento intento) {
        jpa.save(LoginIntentoEntity.builder()
                .id(intento.getId())
                .username(intento.getUsername())
                .ip(intento.getIp())
                .exitoso(intento.isExitoso())
                .creadoEn(intento.getCreadoEn())
                .build());
        entityManager.flush();
    }

    @Override
    public int contarFallidosRecientes(String username) {
        return jpa.contarFallidosRecientes(username);
    }

    @Override
    public void eliminarFallidosPrevios(String username) {
        jpa.eliminarFallidosPrevios(username);
    }
}
