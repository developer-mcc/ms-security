package pe.com.mcco.security.domain.port.out;

import pe.com.mcco.security.domain.enums.CanalNotificacion;

public interface NotificacionPort {

    void enviarCodigo(String destino, CanalNotificacion canal, String codigo);

    void notificar(String destino, CanalNotificacion canal, String mensaje);
}
