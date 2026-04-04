package pe.com.mcco.security.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.com.mcco.security.domain.enums.TipoEvento;
import pe.com.mcco.security.domain.model.AuditLog;
import pe.com.mcco.security.domain.port.out.AuditRepositoryPort;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditApplicationService {

    private final AuditRepositoryPort auditRepository;

    public void registrar(TipoEvento evento, UUID userId, String ip, String userAgent, String detalle) {
        auditRepository.save(AuditLog.builder()
                .id(UUID.randomUUID())
                .evento(evento)
                .userId(userId)
                .ip(ip)
                .userAgent(userAgent)
                .detalle(detalle)
                .creadoEn(LocalDateTime.now())
                .build());
    }
}
