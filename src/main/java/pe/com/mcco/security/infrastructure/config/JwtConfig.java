package pe.com.mcco.security.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Bean
    public KeyPair rsaKeyPair(JwtProperties props, ResourceLoader resourceLoader) throws Exception {
        RSAPrivateKey privateKey = readPrivateKey(props.getPrivateKeyPath(), resourceLoader);
        RSAPublicKey publicKey = readPublicKey(props.getPublicKeyPath(), resourceLoader);
        return new KeyPair(publicKey, privateKey);
    }

    private RSAPrivateKey readPrivateKey(String path, ResourceLoader resourceLoader) throws Exception {
        String pem = readPem(path, resourceLoader);
        pem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                  .replace("-----END PRIVATE KEY-----", "")
                  .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private RSAPublicKey readPublicKey(String path, ResourceLoader resourceLoader) throws Exception {
        String pem = readPem(path, resourceLoader);
        pem = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                  .replace("-----END PUBLIC KEY-----", "")
                  .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private String readPem(String path, ResourceLoader resourceLoader) throws Exception {
        try (InputStream is = resourceLoader.getResource(path).getInputStream()) {
            return new String(is.readAllBytes());
        }
    }
}
