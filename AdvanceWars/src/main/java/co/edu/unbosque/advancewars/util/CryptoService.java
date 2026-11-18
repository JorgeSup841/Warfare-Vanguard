package co.edu.unbosque.advancewars.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Cifra y descifra valores recuperables mediante AES-256-GCM.
 */
@Service
public class CryptoService {
    private static final int NONCE_LENGTH = 12;
    private static final int TAG_BITS = 128;
    private static final String PREFIX = "v1:";
    private final SecretKey key;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Inicializa AES-256 a partir de la clave suministrada externamente.
     */
    public CryptoService(@Value("${app.crypto.key}") String keyBase64) {
        byte[] decoded = Base64.getDecoder().decode(keyBase64);
        if (decoded.length != 32) throw new IllegalArgumentException("La clave AES debe tener 256 bits");
        this.key = new SecretKeySpec(decoded, "AES");
    }

    /**
     * Cifra un valor con AES-GCM y un nonce aleatorio por operación.
     */
    public String encrypt(String value) {
        if (value == null) return null;
        try {
            byte[] nonce = new byte[NONCE_LENGTH];
            secureRandom.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, nonce));
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return PREFIX + Base64.getEncoder().encodeToString(ByteBuffer
                    .allocate(nonce.length + encrypted.length).put(nonce).put(encrypted).array());
        } catch (GeneralSecurityException exception) {
            throw new CryptoException("Error al cifrar datos", exception);
        }
    }

    /**
     * Recupera un valor cifrado comprobando la etiqueta de autenticación.
     */
    public String decrypt(String encoded) {
        if (encoded == null) return null;
        if (!encoded.startsWith(PREFIX)) throw new CryptoException("Formato de cifrado inválido");
        try {
            byte[] all = Base64.getDecoder().decode(encoded.substring(PREFIX.length()));
            if (all.length < NONCE_LENGTH + 16) throw new CryptoException("Datos cifrados incompletos");
            byte[] nonce = Arrays.copyOfRange(all, 0, NONCE_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(all, NONCE_LENGTH, all.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, nonce));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new CryptoException("No se pudo descifrar la información", exception);
        }
    }

    /**
     * Indica un error al procesar un dato cifrado.
     */
    public static class CryptoException extends RuntimeException {
        /**
         * Crea una excepción técnica sin exponer el valor protegido.
         */
        public CryptoException(String message) { super(message); }
        /**
         * Crea una excepción técnica sin exponer el valor protegido.
         */
        public CryptoException(String message, Throwable cause) { super(message, cause); }
    }
}
