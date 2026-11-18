package co.edu.unbosque.advancewars;

import co.edu.unbosque.advancewars.enums.OtpPurpose;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.security.JwtUtil;
import co.edu.unbosque.advancewars.service.OtpHashService;
import co.edu.unbosque.advancewars.util.CryptoService;
import java.util.Base64;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprueba el cifrado, la vinculación de OTP y la validez de JWT.
 */
class PersonaASecurityTests {
    /**
     * Proporciona una clave determinista únicamente para las pruebas unitarias.
     */
    private static String key() {
        byte[] data = new byte[32];
        Arrays.fill(data, (byte) 0x2A);
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Comprueba la aleatoriedad de los cifrados y el rechazo de texto manipulado.
     */
    @Test
    void cifradoAleatorioYAutenticado() {
        CryptoService service = new CryptoService(key());
        String first = service.encrypt("mensaje privado");
        String second = service.encrypt("mensaje privado");
        assertNotEquals(first, second);
        assertEquals("mensaje privado", service.decrypt(first));
        String changed = first.substring(0, first.length() - 3) + "AAAA";
        assertThrows(CryptoService.CryptoException.class, () -> service.decrypt(changed));
    }

    /**
     * Comprueba que el hash OTP depende del código y de su propósito.
     */
    @Test
    void otpNoPermiteOtroCodigo() {
        OtpHashService service = new OtpHashService(key());
        String hashed = service.hash("uuid-ejemplo", OtpPurpose.LOGIN_2FA, "012345");
        assertTrue(service.matches("uuid-ejemplo", OtpPurpose.LOGIN_2FA, "012345", hashed));
        assertFalse(service.matches("uuid-ejemplo", OtpPurpose.LOGIN_2FA, "012346", hashed));
        assertFalse(service.matches("uuid-ejemplo", OtpPurpose.EMAIL_VERIFICATION, "012345", hashed));
    }

    /**
     * Comprueba que modificar las credenciales invalida el JWT previo.
     */
    @Test
    void jwtSeInvalidaCuandoCambiaLaClave() {
        JwtUtil service = new JwtUtil(key(), 3600000);
        User user = new User("usuario", "hash-antiguo", "correo@example.com");
        user.setId(7L);
        user.verifyEmail();
        String token = service.generateToken(user);
        assertTrue(service.belongsTo(service.parse(token), user));
        user.setPassword("hash-nuevo");
        assertFalse(service.belongsTo(service.parse(token), user));
    }
}
