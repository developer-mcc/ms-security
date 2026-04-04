package pe.com.mcco.security.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {

    private String privateKeyPath;
    private String publicKeyPath;
    private int accessTokenExpirationMinutes = 15;
    private int refreshTokenExpirationDays = 7;
}
