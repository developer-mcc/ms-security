package pe.com.mcco.security.infrastructure.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.com.mcco.security.application.dto.RegisterUserCommand;
import pe.com.mcco.security.application.dto.TokenClaims;
import pe.com.mcco.security.domain.model.Usuario;
import pe.com.mcco.security.domain.port.in.AdminUseCase;
import pe.com.mcco.security.domain.port.in.UsuarioUseCase;
import pe.com.mcco.security.domain.model.Rol;
import pe.com.mcco.security.infrastructure.web.dto.request.AdminCreateUserRequest;
import pe.com.mcco.security.infrastructure.web.dto.request.AsignarRolRequest;
import pe.com.mcco.security.infrastructure.web.dto.response.MessageResponse;
import pe.com.mcco.security.infrastructure.web.dto.response.UserCreatedResponse;

import java.util.List;
import java.util.Map;

/**
 * SEQ_08: DELETE /admin/usuarios/{id}/sesiones
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminUseCase adminUseCase;
    private final UsuarioUseCase usuarioUseCase;

    @PostMapping("/usuarios")
    public ResponseEntity<UserCreatedResponse> crearUsuario(
            @Valid @RequestBody AdminCreateUserRequest request) {

        Usuario usuario = usuarioUseCase.crearPorAdmin(RegisterUserCommand.builder()
                .username(request.username())
                .password(request.password())
                .email(request.email())
                .celular(request.celular())
                .build(), request.activo());

        return ResponseEntity.status(201)
                .body(UserCreatedResponse.builder()
                        .id(usuario.getId())
                        .username(usuario.getUsername())
                        .email(usuario.getEmail())
                        .mensaje("Usuario creado por admin exitosamente")
                        .build());
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Map<String, Object>>> listarRoles() {
        List<Rol> roles = adminUseCase.listarRoles();
        var result = roles.stream().map(r -> Map.<String, Object>of(
                "id", r.getId().toString(),
                "codigo", r.getCodigo(),
                "nombre", r.getNombre(),
                "permisos", r.getPermisos().stream().map(p -> Map.of(
                        "codigo", p.getCodigo(),
                        "nombre", p.getNombre(),
                        "modulo", p.getModulo()
                )).toList()
        )).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/usuarios/{id}/roles")
    public ResponseEntity<MessageResponse> asignarRol(
            @PathVariable String id,
            @Valid @RequestBody AsignarRolRequest request,
            @AuthenticationPrincipal TokenClaims claims) {

        adminUseCase.asignarRol(id, request.codigoRol(), claims.userId());
        return ResponseEntity.ok(MessageResponse.builder()
                .mensaje("Rol " + request.codigoRol() + " asignado exitosamente")
                .build());
    }

    @DeleteMapping("/usuarios/{id}/roles/{codigoRol}")
    public ResponseEntity<MessageResponse> quitarRol(
            @PathVariable String id,
            @PathVariable String codigoRol,
            @AuthenticationPrincipal TokenClaims claims) {

        adminUseCase.quitarRol(id, codigoRol, claims.userId());
        return ResponseEntity.ok(MessageResponse.builder()
                .mensaje("Rol " + codigoRol + " removido exitosamente")
                .build());
    }

    @DeleteMapping("/usuarios/{id}/sesiones")
    public ResponseEntity<MessageResponse> revocarSesiones(
            @PathVariable String id,
            @AuthenticationPrincipal TokenClaims claims,
            HttpServletRequest httpRequest) {

        int revocados = adminUseCase.revocarSesionesUsuario(
                id,
                claims.userId(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent"));

        return ResponseEntity.ok(MessageResponse.builder()
                .revocados(revocados)
                .mensaje("Sesiones cerradas exitosamente")
                .build());
    }
}
