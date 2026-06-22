package quant.authservice.util;

import io.jsonwebtoken.Jwts;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import quant.authservice.config.JwtConfig;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private final PrivateKey privateKey;
    private final long accessTokenExpiration;

    public JwtUtil(JwtConfig jwtConfig, ResourceLoader resourceLoader) throws Exception {
        this.accessTokenExpiration = jwtConfig.getAccessTokenExpiration();
        this.privateKey = loadPrivateKey(jwtConfig.getPrivateKey(), resourceLoader);
    }

    public String generateAccessToken(String userId, String role) {
        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(privateKey)
                .compact();
    }

    private PrivateKey loadPrivateKey(String path, ResourceLoader loader) throws Exception {
        InputStream is = loader.getResource(path).getInputStream();
        String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(pem);
        return KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }
}
