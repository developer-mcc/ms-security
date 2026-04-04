package pe.com.mcco.security.domain.port.in;

import pe.com.mcco.security.application.dto.RegisterUserCommand;
import pe.com.mcco.security.domain.model.Usuario;

public interface UsuarioUseCase {

    Usuario registrar(RegisterUserCommand command);

    Usuario crearPorAdmin(RegisterUserCommand command, boolean activo);
}
