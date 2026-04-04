package pe.com.mcco.security.infrastructure.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.com.mcco.security.domain.enums.CanalNotificacion;
import pe.com.mcco.security.domain.port.out.NotificacionPort;

/**
 * Adaptador de notificaciones.
 * TODO: integrar con servicios reales de Email (SES/SMTP), SMS (Twilio), WhatsApp (Meta API).
 * Por ahora loguea las notificaciones.
 */
@Slf4j
@Component
public class NotificacionAdapter implements NotificacionPort {

    @Override
    public void enviarCodigo(String destino, CanalNotificacion canal, String codigo) {
        log.info("[NOTIFICACION] Enviando codigo {} por {} a {}", codigo, canal, enmascarar(destino));
        // TODO: implementar segun canal
        //   EMAIL -> EmailService.enviar(destino, "Codigo recuperacion", codigo)
        //   SMS   -> SmsService.enviar(destino, "Tu codigo es " + codigo)
        //   WHATSAPP -> WhatsAppService.enviarTemplate(destino, codigo)
    }

    @Override
    public void notificar(String destino, CanalNotificacion canal, String mensaje) {
        log.info("[NOTIFICACION] {} por {} a {}: {}", canal, canal, enmascarar(destino), mensaje);
    }

    private String enmascarar(String destino) {
        if (destino == null || destino.length() < 4) return "***";
        return destino.substring(0, 3) + "***";
    }
}
