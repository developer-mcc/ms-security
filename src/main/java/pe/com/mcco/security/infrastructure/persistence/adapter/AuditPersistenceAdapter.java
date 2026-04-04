package pe.com.mcco.security.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.com.mcco.security.domain.model.AuditLog;
import pe.com.mcco.security.domain.port.out.AuditRepositoryPort;
import pe.com.mcco.security.infrastructure.persistence.entity.AuditLogEntity;
import pe.com.mcco.security.infrastructure.persistence.repository.AuditLogJpaRepository;

@Component
@RequiredArgsConstructor
public class AuditPersistenceAdapter implements AuditRepositoryPort {

    private final AuditLogJpaRepository jpa;

    @Override
    public void save(AuditLog log) {
        jpa.save(AuditLogEntity.builder()
                .id(log.getId())
                .evento(log.getEvento())
                .userId(log.getUserId())
                .ip(log.getIp())
                .userAgent(log.getUserAgent())
                .detalle(log.getDetalle())
                .creadoEn(log.getCreadoEn())
                .build());
    }
}
