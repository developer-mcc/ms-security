package pe.com.mcco.security.application.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.com.mcco.security.application.dto.TokenClaims;
import pe.com.mcco.security.domain.enums.EstadoToken;
import pe.com.mcco.security.domain.enums.TipoToken;
import pe.com.mcco.security.domain.exception.TokenExpiradoException;
import pe.com.mcco.security.domain.exception.TokenInvalidoException;
import pe.com.mcco.security.domain.exception.TokenRevocadoException;
import pe.com.mcco.security.domain.model.TokenSesion;
import pe.com.mcco.security.domain.port.out.TokenCachePort;
import pe.com.mcco.security.domain.port.out.TokenSesionRepositoryPort;
import pe.com.mcco.security.infrastructure.config.JwtProperties;

import pe.com.mcco.security.domain.model.Usuario;
import pe.com.mcco.security.domain.port.out.UsuarioRepositoryPort;

import java.security.KeyPair;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenApplicationService {

    private final TokenSesionRepositoryPort tokenRepository;
    private final TokenCachePort tokenCache;
    private final UsuarioRepositoryPort usuarioRepository;
    private final JwtProperties jwtProperties;
    private final KeyPair rsaKeyPair;

    // --- Emitir access token JWT (SEQ_01 linea 79) ---
    public String emitirAccessToken(UUID userId, String branchId) {
        String jti = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(jwtProperties.getAccessTokenExpirationMinutes() * 60L);

        String jwt = Jwts.builder()
                .id(jti)
                .subject(userId.toString())
                .claim("branchId", branchId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(rsaKeyPair.getPrivate())
                .compact();

        tokenRepository.save(TokenSesion.builder()
                .id(UUID.randomUUID())
                .jti(jti)
                .userId(userId)
                .tipo(TipoToken.ACCESS)
                .estado(EstadoToken.ACTIVO)
                .branchId(branchId)
                .emitidoEn(LocalDateTime.ofInstant(now, ZoneId.systemDefault()))
                .expiraEn(LocalDateTime.ofInstant(exp, ZoneId.systemDefault()))
                .build());

        return jwt;
    }

    // --- Emitir refresh token opaco (SEQ_01 linea 83-86) ---
    public String emitirRefreshToken(UUID userId) {
        String refreshToken = UUID.randomUUID().toString();
        long ttlSeconds = jwtProperties.getRefreshTokenExpirationDays() * 86400L;

        tokenRepository.save(TokenSesion.builder()
                .id(UUID.randomUUID())
                .jti(refreshToken)
                .userId(userId)
                .tipo(TipoToken.REFRESH)
                .estado(EstadoToken.ACTIVO)
                .emitidoEn(LocalDateTime.now())
                .expiraEn(LocalDateTime.now().plusDays(jwtProperties.getRefreshTokenExpirationDays()))
                .build());

        tokenCache.guardarRefreshToken(userId.toString(), refreshToken, ttlSeconds);
        return refreshToken;
    }

    // --- Validar token en cada request (SEQ_02) ---
    public TokenClaims validarToken(String jwt) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(rsaKeyPair.getPublic())
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new TokenExpiradoException();
        } catch (JwtException e) {
            throw new TokenInvalidoException("firma invalida");
        }

        String jti = claims.getId();

        // Verificar blacklist en cache (SEQ_02 linea 132)
        if (tokenCache.isBlacklisted(jti)) {
            throw new TokenRevocadoException();
        }

        // Doble verificacion en BD (SEQ_02 linea 148-149)
        tokenRepository.findByJtiAndEstado(jti, EstadoToken.ACTIVO).orElseThrow(() -> {
            // Re-sincronizar cache con BD (SEQ_02 linea 153-154)
            long ttl = claims.getExpiration().toInstant().getEpochSecond() - Instant.now().getEpochSecond();
            if (ttl > 0) {
                tokenCache.addToBlacklist(jti, ttl);
            }
            return new TokenRevocadoException();
        });

        // Actualizar ultimo uso (SEQ_02 linea 158)
        tokenRepository.updateUltimoUso(jti);

        // Roles y permisos siempre frescos desde BD
        String userId = claims.getSubject();
        Usuario usuario = usuarioRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new TokenInvalidoException("usuario no encontrado"));

        return TokenClaims.builder()
                .userId(userId)
                .branchId(claims.get("branchId", String.class))
                .roles(usuario.getCodigosRoles())
                .permisos(usuario.getPermisos())
                .jti(jti)
                .build();
    }

    // --- Revocar un token especifico (SEQ_03 logout) ---
    public void revocarToken(String jti, long expEpochSeconds, EstadoToken estado) {
        tokenRepository.updateEstado(jti, estado);
        long ttl = expEpochSeconds - Instant.now().getEpochSecond();
        if (ttl > 0) {
            tokenCache.addToBlacklist(jti, ttl);
        }
    }

    // --- Revocar todos los tokens de un usuario (SEQ_06, SEQ_08) ---
    public int revocarTodosTokens(UUID userId, EstadoToken estado) {
        List<TokenSesion> activos = tokenRepository.findActivosByUserId(userId);
        for (TokenSesion token : activos) {
            long ttl = token.getExpiraEn().atZone(ZoneId.systemDefault()).toEpochSecond()
                    - Instant.now().getEpochSecond();
            if (ttl > 0) {
                tokenCache.addToBlacklist(token.getJti(), ttl);
            }
        }
        tokenRepository.revocarTodosPorUsuario(userId, estado);
        tokenCache.eliminarRefreshToken(userId.toString());
        return activos.size();
    }

    // --- Validar refresh token (SEQ_04 linea 233) ---
    public UUID validarRefreshToken(String refreshToken) {
        // Buscar token activo en BD
        TokenSesion token = tokenRepository.findByJtiAndEstado(refreshToken, EstadoToken.ACTIVO)
                .orElseThrow(() -> new pe.com.mcco.security.domain.exception.RefreshTokenInvalidoException());

        // Verificar que exista en cache tambien
        tokenCache.obtenerRefreshToken(token.getUserId().toString())
                .orElseThrow(() -> new pe.com.mcco.security.domain.exception.RefreshTokenInvalidoException());

        return token.getUserId();
    }

    // --- Rotacion atomica de refresh token (SEQ_04 linea 243-248) ---
    public void revocarRefreshToken(String refreshToken, UUID userId) {
        tokenCache.eliminarRefreshToken(userId.toString());
        tokenRepository.updateEstado(refreshToken, EstadoToken.REVOCADO_ROTACION);
    }
}
