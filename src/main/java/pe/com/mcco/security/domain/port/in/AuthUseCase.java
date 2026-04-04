package pe.com.mcco.security.domain.port.in;

import pe.com.mcco.security.application.dto.AuthResult;
import pe.com.mcco.security.application.dto.LoginCommand;
import pe.com.mcco.security.application.dto.TokenClaims;

public interface AuthUseCase {

    AuthResult login(LoginCommand command);

    void logout(String jti, long expSeconds, String userId, String ip, String userAgent);

    void logoutByIdle(String jti, long expSeconds, String userId, String ip, String userAgent);

    AuthResult refresh(String refreshToken);

    TokenClaims validarToken(String jwt);
}
