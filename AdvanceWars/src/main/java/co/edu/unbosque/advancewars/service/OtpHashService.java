package co.edu.unbosque.advancewars.service;

import co.edu.unbosque.advancewars.enums.OtpPurpose;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OtpHashService {
    private final byte[] pepper;

    public OtpHashService(@Value("${app.otp.pepper}") String pepperBase64) {
        pepper = Base64.getDecoder().decode(pepperBase64);
        if (pepper.length < 32) throw new IllegalArgumentException("APP_OTP_PEPPER demasiado corto");
    }

    public String hash(String challengeId, OtpPurpose purpose, String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(pepper, "HmacSHA256"));
            byte[] output = mac.doFinal((challengeId + ":" + purpose.name() + ":" + code)
                    .getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(output);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("No se puede validar OTP", exception);
        }
    }

    public boolean matches(String challengeId, OtpPurpose purpose, String code, String expectedHash) {
        return MessageDigest.isEqual(hash(challengeId, purpose, code).getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8));
    }
}
