package co.edu.unbosque.advancewars.controller;

import co.edu.unbosque.advancewars.service.PdfReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone la descarga de reportes de partidas para administradores.
 */
@RestController
@RequestMapping("/api/admin/games")
public class ReportController {
    private final PdfReportService reports;

    /**
     * Inyecta el servicio de generación de reportes.
     */
    public ReportController(PdfReportService reports) { this.reports = reports; }

    /**
     * Genera una descarga PDF para la partida solicitada.
     */
    @GetMapping(value = "/{gameId}/report.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> report(@PathVariable Long gameId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=advancewars-" + gameId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(reports.create(gameId));
    }
}
