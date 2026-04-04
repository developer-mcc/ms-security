package pe.com.mcco.security.domain.port.in;

import pe.com.mcco.security.domain.model.Rol;

import java.util.List;

public interface AdminUseCase {

    int revocarSesionesUsuario(String targetUserId, String adminId, String ip, String userAgent);

    void asignarRol(String usuarioId, String codigoRol, String adminId);

    void quitarRol(String usuarioId, String codigoRol, String adminId);

    List<Rol> listarRoles();
}
