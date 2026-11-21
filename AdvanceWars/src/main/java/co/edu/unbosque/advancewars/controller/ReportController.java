package co.edu.unbosque.advancewars.controller;

import co.edu.unbosque.advancewars.service.PdfReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Expone el reporte PDF de partidas propias o de administración. */
@RestController
public class ReportController {
	private final PdfReportService reports;

	/** Inyecta el servicio de reportes. */
	public ReportController(PdfReportService reports) {
		this.reports = reports;
	}

	/** Devuelve el PDF comprobando la propiedad de la partida en el servicio. */
	@GetMapping(value = { "/api/games/{gameId}/report.pdf",
			"/api/admin/games/{gameId}/report.pdf" }, produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<byte[]> report(@PathVariable("gameId") Long gameId, Authentication authentication) {
		boolean administrator = authentication.getAuthorities().stream()
				.anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
		byte[] pdf = reports.create(gameId, authentication.getName(), administrator);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=advancewars-" + gameId + ".pdf")
				.contentType(MediaType.APPLICATION_PDF).body(pdf);
	}
}
