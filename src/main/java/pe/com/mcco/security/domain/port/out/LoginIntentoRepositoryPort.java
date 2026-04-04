package pe.com.mcco.security.domain.port.out;

import pe.com.mcco.security.domain.model.LoginIntento;

public interface LoginIntentoRepositoryPort {

    void save(LoginIntento intento);

    int contarFallidosRecientes(String username);

    void eliminarFallidosPrevios(String username);
}
