package co.edu.unbosque.advancewars.service;

import co.edu.unbosque.advancewars.exception.ResourceNotFoundException;
import co.edu.unbosque.advancewars.model.GameLog;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Player;
import co.edu.unbosque.advancewars.repository.GameLogRepository;
import co.edu.unbosque.advancewars.repository.GameStateRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Genera un PDF con el resumen y el historial de una partida.
 */
@Service
public class PdfReportService {
    private final GameStateRepository games;
    private final GameLogRepository logs;

    /**
     * Inyecta los repositorios de partidas y de eventos.
     */
    public PdfReportService(GameStateRepository games, GameLogRepository logs) {
        this.games = games;
        this.logs = logs;
    }

    /**
     * Construye un documento PDF a partir de una partida y sus eventos.
     */
    @Transactional(readOnly = true)
    public byte[] create(Long gameId) {
        GameState game = games.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Partida no encontrada"));
        List<String> lines = new ArrayList<>();
        lines.add("ADVANCEWARS - REPORTE DE PARTIDA #" + game.getId());
        lines.add("Estado: " + game.getStatus() + " | Ronda: " + game.getCurrentRound());
        lines.add("Semilla: " + game.getSeed() + " | Inicio: " + game.getStartedAt());
        lines.add("Ganador (orden): " + game.getWinnerPlayerOrder());
        lines.add("Jugadores:");
        for (Player player : game.getPlayers()) {
            lines.add("  " + player.getTurnOrder() + ". " + player.getName() + " - " + player.getStatus());
        }
        lines.add("Historial:");
        List<GameLog> events = logs.findAllByGameState_IdOrderByCreatedAtAscIdAsc(gameId);
        for (GameLog event : events) {
            lines.add("Ronda " + event.getRoundNumber() + " - " + event.getEventType()
                    + " - " + event.getDescription());
        }
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDPage page = null;
            PDPageContentStream stream = null;
            float y = 0;
            try {
                for (String line : lines) {
                    for (String section : split(ascii(line), 82)) {
                        if (page == null || y < 45) {
                            if (stream != null) stream.close();
                            page = new PDPage();
                            document.addPage(page);
                            stream = new PDPageContentStream(document, page);
                            y = page.getMediaBox().getHeight() - 50;
                        }
                        stream.beginText();
                        stream.setFont(font, 10);
                        stream.newLineAtOffset(45, y);
                        stream.showText(section);
                        stream.endText();
                        y -= 15;
                    }
                }
            } finally {
                if (stream != null) stream.close();
            }
            document.save(out);
            return out.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo generar el reporte", exception);
        }
    }

    /**
     * Prepara texto seguro para la fuente básica del PDF.
     */
    private String ascii(String value) {
        String normalized = Normalizer.normalize(value.replaceAll("[\\r\\n\\t]", " "),
                Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return normalized.replaceAll("[^\\x20-\\x7E]", "?");
    }

    /**
     * Divide una línea extensa para su representación en el PDF.
     */
    private List<String> split(String value, int max) {
        List<String> result = new ArrayList<>();
        if (value.isBlank()) return List.of(" ");
        for (int i = 0; i < value.length(); i += max) {
            result.add(value.substring(i, Math.min(value.length(), i + max)));
        }
        return result;
    }
}
