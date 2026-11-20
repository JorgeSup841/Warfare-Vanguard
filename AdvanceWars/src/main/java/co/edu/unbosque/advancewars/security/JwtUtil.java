package co.edu.unbosque.advancewars.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import co.edu.unbosque.advancewars.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Genera, interpreta y valida JWT firmados para usuarios de AdvanceWars.
 * Vincula cada token a la versión vigente de las credenciales de la cuenta.
 */
@Component
public class JwtUtil {
    private final SecretKey signingKey;
    private final byte[] fingerprintKey;
    private final long expirationMillis;

    /**
     * Inicializa la firma HMAC a partir de una clave Base64 del entorno.
     *
     * @param secretBase64 clave secreta codificada en Base64
     * @param expirationMillis duración de los tokens en milisegundos
     * @throws IllegalArgumentException si la configuración es inválida
     */
    public JwtUtil(@Value("${jwt.secret}") String secretBase64,
            @Value("${jwt.expiration-ms}") long expirationMillis) {
        byte[] raw = Decoders.BASE64.decode(secretBase64);
        if (raw.length < 32 || expirationMillis < 60000) {
            throw new IllegalArgumentException("Configuración JWT inválida");
        }
        this.signingKey = Keys.hmacShaKeyFor(raw);
        this.fingerprintKey = raw.clone();
        this.expirationMillis = expirationMillis;
    }

    /**
     * Emite un JWT con identidad, rol y huella de las credenciales actuales.
     *
     * @param user usuario autenticado tras superar el segundo factor
     * @return JWT firmado y con fecha de expiración
     */
    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(user.getUsername())
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
                .claim("roles", java.util.List.of("ROLE_" + user.getRole().name()))
                .claim("cv", credentialFingerprint(user))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Analiza un JWT y comprueba su firma y vigencia.
     *
     * @param token JWT recibido
     * @return declaraciones verificadas del token
     * @throws io.jsonwebtoken.JwtException si el token es inválido
     */
    public Claims parse(String token) {
        return Jwts.parserBuilder().setSigningKey(signingKey).build()
                .parseClaimsJws(token).getBody();
    }

    /**
     * Obtiene el nombre de usuario contenido en un JWT válido.
     *
     * @param token token recibido
     * @return nombre de usuario autenticado
     */
    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    /**
     * Obtiene el instante de vencimiento del JWT.
     *
     * @param token token recibido
     * @return fecha de expiración
     */
    public Date extractExpiration(String token) {
        return parse(token).getExpiration();
    }

    /**
     * Extrae el rol declarado en el JWT firmado.
     *
     * @param token token recibido
     * @return nombre del rol incluido en el token
     */
    public String extractRole(String token) {
        return parse(token).get("role", String.class);
    }

    /**
     * Comprueba que el JWT pertenezca a un usuario aún autorizado.
     *
     * @param token token recibido
     * @param user usuario actual obtenido desde la base de datos
     * @return verdadero si firma, vigencia y estado de cuenta son válidos
     */
    public boolean validateToken(String token, User user) {
        return validateClaims(parse(token), user);
    }

    /**
     * Verifica el estado del usuario y la relación con el JWT analizado.
     *
     * @param claims declaraciones extraídas de un token ya verificado
     * @param user usuario actual de la base de datos
     * @return verdadero si el usuario puede autenticarse con ese token
     */
    public boolean validateClaims(Claims claims, User user) {
        return user != null && user.isEnabled() && user.isEmailVerified()
                && user.isAccountNonLocked() && user.isAccountNonExpired()
                && user.isCredentialsNonExpired() && belongsTo(claims, user);
    }

    /**
     * Comprueba identidad y versión de credenciales del usuario.
     *
     * @param claims declaraciones del token validado
     * @param user usuario actual de la base de datos
     * @return verdadero si el token corresponde a esta cuenta y contraseña
     */
    public boolean belongsTo(Claims claims, User user) {
        Object id = claims.get("uid");
        Object fingerprint = claims.get("cv");
        return id instanceof Number && user.getId() != null
                && ((Number) id).longValue() == user.getId()
                && user.getUsername().equals(claims.getSubject())
                && fingerprint instanceof String
                && constantTimeEquals((String) fingerprint, credentialFingerprint(user));
    }

    /**
     * Calcula una huella HMAC para invalidar tokens al cambiar la contraseña.
     *
     * @param user cuenta con identificador y contraseña cifrada mediante BCrypt
     * @return huella hexadecimal de las credenciales vigentes
     */
    private String credentialFingerprint(User user) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(fingerprintKey, "HmacSHA256"));
            byte[] bytes = mac.doFinal((user.getId() + ":" + user.getPassword())
                    .getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("No se puede calcular la versión de credenciales", exception);
        }
    }

    /**
     * Compara las huellas sin depender del primer carácter diferente.
     *
     * @param left primera huella
     * @param right segunda huella
     * @return verdadero si las huellas son iguales
     */
    private boolean constantTimeEquals(String left, String right) {
        return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8));
    }
}
