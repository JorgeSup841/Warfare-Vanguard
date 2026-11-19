package co.edu.unbosque.advancewars.security;

import co.edu.unbosque.advancewars.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Date;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    private final SecretKey signingKey;
    private final byte[] fingerprintKey;
    private final long expirationMillis;

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

    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
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

    public Claims parse(String token) {
        return Jwts.parserBuilder().setSigningKey(signingKey).build()
                .parseClaimsJws(token).getBody();
    }

    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return parse(token).getExpiration();
    }

    public String extractRole(String token) {
        return parse(token).get("role", String.class);
    }

    public boolean validateToken(String token, User user) {
        return validateClaims(parse(token), user);
    }

    public boolean validateClaims(Claims claims, User user) {
        return user != null && user.isEnabled() && user.isEmailVerified()
                && user.isAccountNonLocked() && user.isAccountNonExpired()
                && user.isCredentialsNonExpired() && belongsTo(claims, user);
    }

    public boolean belongsTo(Claims claims, User user) {
        Object id = claims.get("uid");
        Object fingerprint = claims.get("cv");
        return id instanceof Number && user.getId() != null
                && ((Number) id).longValue() == user.getId()
                && user.getUsername().equals(claims.getSubject())
                && fingerprint instanceof String
                && constantTimeEquals((String) fingerprint, credentialFingerprint(user));
    }

    private String credentialFingerprint(User user) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(fingerprintKey, "HmacSHA256"));
            byte[] bytes = mac.doFinal((user.getId() + ":" + user.getPassword())
                    .getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(bytes);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("No se puede calcular la versión de credenciales", exception);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        return java.security.MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8));
    }
}
