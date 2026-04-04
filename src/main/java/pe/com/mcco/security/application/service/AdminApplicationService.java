package pe.com.mcco.security.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcco.security.domain.enums.CodigoRol;
import pe.com.mcco.security.domain.enums.EstadoToken;
import pe.com.mcco.security.domain.enums.TipoEvento;
import pe.com.mcco.security.domain.exception.UsuarioNoEncontradoException;
import pe.com.mcco.security.domain.model.Rol;
import pe.com.mcco.security.domain.port.in.AdminUseCase;
import pe.com.mcco.security.domain.port.out.NotificacionPort;
import pe.com.mcco.security.domain.port.out.RolRepositoryPort;
import pe.com.mcco.security.domain.port.out.UsuarioRepositoryPort;
import pe.com.mcco.security.domain.enums.CanalNotificacion;

import java.util.List;
import java.util.UUID;

/**
 * Implementa SEQ_08: Admin revoca todas las sesiones de un usuario.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminApplicationService implements AdminUseCase {

    private final TokenApplicationService tokenService;
    private final AuditApplicationService auditService;
    private final NotificacionPort notificacion;
    private final UsuarioRepositoryPort usuarioRepository;
    private final RolRepositoryPort rolRepository;

    @Override
    public int revocarSesionesUsuario(String targetUserId, String adminId, String ip, String userAgent) {
        UUID targetUid = UUID.fromString(targetUserId);

        // Revocar todos los tokens (SEQ_08 linea 517-527)
        int revocados = tokenService.revocarTodosTokens(targetUid, EstadoToken.REVOCADO_ADMIN);

        // Notificar al usuario (SEQ_08 linea 530-531)
        usuarioRepository.findById(targetUid).ifPresent(usuario -> {
            String destino = usuario.getEmail() != null ? usuario.getEmail() : usuario.getCelular();
            if (destino != null) {
                notificacion.notificar(destino, CanalNotificacion.EMAIL,
                        "Tu sesion fue cerrada por el administrador");
            }
        });

        // Auditar (SEQ_08 linea 533-535)
        auditService.registrar(TipoEvento.SESIONES_REVOCADAS_ADMIN,
                UUID.fromString(adminId), ip, userAgent,
                "{\"targetUserId\": \"" + targetUserId + "\", \"tokensRevocados\": " + revocados + "}");

        return revocados;
    }

    @Override
    public void asignarRol(String usuarioId, String codigoRol, String adminId) {
        validarCodigoRol(codigoRol);

        UUID uid = UUID.fromString(usuarioId);
        usuarioRepository.findById(uid).orElseThrow(UsuarioNoEncontradoException::new);

        Rol rol = rolRepository.findByCodigo(codigoRol)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + codigoRol));

        rolRepository.asignarRolAUsuario(uid, rol.getId(), UUID.fromString(adminId));

        auditService.registrar(TipoEvento.SESIONES_REVOCADAS_ADMIN,
                UUID.fromString(adminId), null, null,
                "{\"accion\": \"ASIGNAR_ROL\", \"usuario\": \"" + usuarioId + "\", \"rol\": \"" + codigoRol + "\"}");
    }

    @Override
    public void quitarRol(String usuarioId, String codigoRol, String adminId) {
        validarCodigoRol(codigoRol);

        UUID uid = UUID.fromString(usuarioId);
        usuarioRepository.findById(uid).orElseThrow(UsuarioNoEncontradoException::new);

        Rol rol = rolRepository.findByCodigo(codigoRol)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + codigoRol));

        rolRepository.quitarRolDeUsuario(uid, rol.getId());

        auditService.registrar(TipoEvento.SESIONES_REVOCADAS_ADMIN,
                UUID.fromString(adminId), null, null,
                "{\"accion\": \"QUITAR_ROL\", \"usuario\": \"" + usuarioId + "\", \"rol\": \"" + codigoRol + "\"}");
    }

    @Override
    public List<Rol> listarRoles() {
        return rolRepository.findAllActivos();
    }

    private void validarCodigoRol(String codigoRol) {
        try {
            CodigoRol.valueOf(codigoRol);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Rol invalido: " + codigoRol + ". Valores permitidos: "
                            + java.util.Arrays.toString(CodigoRol.values()));
        }
    }
}
