package pe.com.mcco.security.domain.port.out;

import pe.com.mcco.security.domain.model.AuditLog;

public interface AuditRepositoryPort {

    void save(AuditLog log);
}
