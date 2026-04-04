package pe.com.mcco.security.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.mcco.security.application.dto.RegisterUserCommand;
import pe.com.mcco.security.domain.exception.PasswordDebilException;
import pe.com.mcco.security.domain.exception.UsuarioYaExisteException;
import pe.com.mcco.security.domain.model.Usuario;
import pe.com.mcco.security.domain.port.in.UsuarioUseCase;
import pe.com.mcco.security.domain.port.out.UsuarioRepositoryPort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioApplicationService implements UsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Usuario registrar(RegisterUserCommand command) {
        validar(command);
        return guardar(command, true);
    }

    @Override
    public Usuario crearPorAdmin(RegisterUserCommand command, boolean activo) {
        validar(command);
        return guardar(command, activo);
    }

    private void validar(RegisterUserCommand command) {
        if (usuarioRepository.existsByUsername(command.username())) {
            throw new UsuarioYaExisteException("username");
        }
        if (command.email() != null && usuarioRepository.existsByEmail(command.email())) {
            throw new UsuarioYaExisteException("email");
        }
        validarFortaleza(command.password());
    }

    private Usuario guardar(RegisterUserCommand command, boolean activo) {
        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .username(command.username())
                .passwordHash(passwordEncoder.encode(command.password()))
                .email(command.email())
                .celular(command.celular())
                .activo(activo)
                .build();
        return usuarioRepository.save(usuario);
    }

    private void validarFortaleza(String password) {
        List<String> errores = new ArrayList<>();
        if (password.length() < 12) errores.add("Minimo 12 caracteres");
        if (!password.matches(".*[A-Z].*")) errores.add("Debe contener al menos una mayuscula");
        if (!password.matches(".*[a-z].*")) errores.add("Debe contener al menos una minuscula");
        if (!password.matches(".*\\d.*")) errores.add("Debe contener al menos un numero");
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            errores.add("Debe contener al menos un caracter especial");
        }
        if (!errores.isEmpty()) throw new PasswordDebilException(errores);
    }
}
