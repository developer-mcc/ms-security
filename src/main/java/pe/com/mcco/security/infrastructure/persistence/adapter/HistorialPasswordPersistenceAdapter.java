package pe.com.mcco.security.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcco.security.domain.model.HistorialPassword;
import pe.com.mcco.security.domain.port.out.HistorialPasswordRepositoryPort;
import pe.com.mcco.security.infrastructure.persistence.entity.HistorialPasswordEntity;
import pe.com.mcco.security.infrastructure.persistence.repository.HistorialPasswordJpaRepository;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HistorialPasswordPersistenceAdapter implements HistorialPasswordRepositoryPort {

    private final HistorialPasswordJpaRepository jpa;

    @Override
    public void save(HistorialPassword historial) {
        jpa.save(HistorialPasswordEntity.builder()
                .id(historial.getId())
                .userId(historial.getUserId())
                .passwordHash(historial.getPasswordHash())
                .cambiadoEn(historial.getCambiadoEn())
                .build());
    }

    @Override
    public List<HistorialPassword> findRecientesByUserId(UUID userId, int limite) {
        return jpa.findRecientesByUserId(userId, limite).stream()
                .map(e -> HistorialPassword.builder()
                        .id(e.getId())
                        .userId(e.getUserId())
                        .passwordHash(e.getPasswordHash())
                        .cambiadoEn(e.getCambiadoEn())
                        .build())
                .toList();
    }
}
