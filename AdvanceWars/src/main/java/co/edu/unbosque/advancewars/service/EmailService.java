package co.edu.unbosque.advancewars.service;

import co.edu.unbosque.advancewars.enums.OtpPurpose;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envía por SMTP los códigos de verificación de la aplicación.
 */
@Service
public class EmailService {
	private final JavaMailSender mailSender;
	private final String from;
	private final long otpTtlSeconds;

	/**
	 * Inyecta el transportador SMTP y el remitente configurado.
	 */
	public EmailService(JavaMailSender mailSender, @Value("${app.mail.from}") String from,
			@Value("${app.otp.ttl-seconds}") long otpTtlSeconds) {
		this.mailSender = mailSender;
		this.from = from;
		this.otpTtlSeconds = otpTtlSeconds;
	}

	/**
	 * Envía el código OTP al correo del destinatario con el asunto adecuado.
	 */
	public void sendOtp(OtpService.IssuedOtp issued) {
		String subject = switch (issued.purpose()) {
		case EMAIL_VERIFICATION -> "AdvanceWars - Verifica tu correo";
		case LOGIN_2FA -> "AdvanceWars - Código de acceso";
		case PASSWORD_RESET -> "AdvanceWars - Restablecer contraseña";
		};
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(from);
		message.setTo(issued.email());
		message.setSubject(subject);
		message.setText("Tu código de AdvanceWars es: " + issued.code()
				+ (issued.purpose() == OtpPurpose.PASSWORD_RESET
						? "\nIdentificador para restablecer contraseña: " + issued.challengeId()
						: "")
				+ "\nVálido durante " + ((otpTtlSeconds + 59) / 60)
				+ " minutos como máximo. Si no lo solicitaste, ignora este mensaje.");
		mailSender.send(message);
	}
}
