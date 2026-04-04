package pe.com.mcco.security.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcco.security.domain.exception.UsuarioBloqueadoException;
import pe.com.mcco.security.domain.model.LoginIntento;
import pe.com.mcco.security.domain.port.out.LoginIntentoRepositoryPort;
import pe.com.mcco.security.domain.port.out.TokenCachePort;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginAttemptApplicationService {

    private final LoginIntentoRepositoryPort loginIntentoRepository;
    private final TokenCachePort tokenCache;

    @Value("${app.security.max-login-attempts}")
    private int maxIntentos;

    @Value("${app.security.block-duration-minutes}")
    private int bloqueoMinutos;

    public void verificarBloqueo(String username) {
        Optional<Long> bloqueadoHasta = tokenCache.obtenerBloqueadoHastaEpoch(username);
        if (bloqueadoHasta.isPresent()) {
            LocalDateTime hasta = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(bloqueadoHasta.get()), ZoneId.systemDefault());
            throw new UsuarioBloqueadoException(hasta);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarIntento(String username, String ip, boolean exitoso) {
        loginIntentoRepository.save(LoginIntento.builder()
                .id(UUID.randomUUID())
                .username(username)
                .ip(ip)
                .exitoso(exitoso)
                .creadoEn(LocalDateTime.now())
                .build());
    }

    public int contarFallidos(String username) {
        return loginIntentoRepository.contarFallidosRecientes(username);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void bloquearSiExcedeMaximo(String username) {
        int fallidos = contarFallidos(username);
        if (fallidos >= maxIntentos) {
            long ttlSeconds = (long) bloqueoMinutos * 60;
            tokenCache.bloquearUsuario(username, ttlSeconds);
        }
    }

    public boolean estaEnLimite(String username) {
        return contarFallidos(username) >= maxIntentos;
    }

    public int intentosRestantes(String username) {
        return Math.max(0, maxIntentos - contarFallidos(username));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void limpiarIntentosFallidos(String username) {
        loginIntentoRepository.eliminarFallidosPrevios(username);
        tokenCache.desbloquearUsuario(username);
    }
}
