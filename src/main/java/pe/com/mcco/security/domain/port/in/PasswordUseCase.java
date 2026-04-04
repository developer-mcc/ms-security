package pe.com.mcco.security.domain.port.in;

import pe.com.mcco.security.application.dto.ChangePasswordCommand;
import pe.com.mcco.security.application.dto.ForgotPasswordCommand;
import pe.com.mcco.security.application.dto.ResetPasswordCommand;

public interface PasswordUseCase {

    void cambiarPassword(ChangePasswordCommand command);

    void solicitarReset(ForgotPasswordCommand command);

    void resetearPassword(ResetPasswordCommand command);
}
