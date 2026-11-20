package co.edu.unbosque.advancewars.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.advancewars.dto.AdminGameDTO;
import co.edu.unbosque.advancewars.dto.AdminGameLogDTO;
import co.edu.unbosque.advancewars.dto.MapDTO;
import co.edu.unbosque.advancewars.dto.UnitDTO;
import co.edu.unbosque.advancewars.exception.ResourceNotFoundException;
import co.edu.unbosque.advancewars.model.GameLog;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Unit;
import co.edu.unbosque.advancewars.repository.GameLogRepository;
import co.edu.unbosque.advancewars.repository.GameStateRepository;
import co.edu.unbosque.advancewars.repository.PropertyRepository;
import co.edu.unbosque.advancewars.repository.UnitRepository;

/** Consultas globales de partidas reservadas a ADMIN. */
@Service
public class AdminGameService {
    private final GameStateRepository games;
    private final GameLogRepository logs;
    private final PropertyRepository properties;
    private final UnitRepository units;

    public AdminGameService(GameStateRepository games, GameLogRepository logs,
            PropertyRepository properties, UnitRepository units) {
        this.games = games;
        this.logs = logs;
        this.properties = properties;
        this.units = units;
    }

    private GameState load(Long id) {
        Optional<GameState> found = games.findById(id);
        if (found.isEmpty()) {
            throw new ResourceNotFoundException("Partida no encontrada");
        }
        return found.get();
    }

    @Transactional(readOnly = true)
    public List<AdminGameDTO> list() {
        List<AdminGameDTO> result = new ArrayList<>();
        for (GameState game : games.findAllByOrderByCreatedAtDesc()) {
            result.add(new AdminGameDTO(game));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public AdminGameDTO find(Long id) { return new AdminGameDTO(load(id)); }

    @Transactional(readOnly = true)
    public MapDTO map(Long id) {
        GameState game = load(id);
        return new MapDTO(game, properties.findAllByTile_GameMap_Id(game.getGameMap().getId()));
    }

    @Transactional(readOnly = true)
    public List<UnitDTO> units(Long id) {
        load(id);
        List<UnitDTO> result = new ArrayList<>();
        for (Unit unit : units.findAllByOwner_GameState_Id(id)) {
            result.add(new UnitDTO(unit));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<AdminGameLogDTO> history(Long id) {
        load(id);
        List<AdminGameLogDTO> result = new ArrayList<>();
        for (GameLog log : logs.findAllByGameState_IdOrderByCreatedAtAscIdAsc(id)) {
            result.add(new AdminGameLogDTO(log));
        }
        return result;
    }

    /** Borra dependencias antes de mapa y jugadores para respetar sus claves foraneas. */
    @Transactional
    public void delete(Long id) {
        GameState game = load(id);
        logs.deleteAll(logs.findAllByGameState_IdOrderByCreatedAtAscIdAsc(id));
        logs.flush();
        units.deleteAll(units.findAllByOwner_GameState_Id(id));
        units.flush();
        properties.deleteAll(properties.findAllByTile_GameMap_Id(game.getGameMap().getId()));
        properties.flush();
        games.delete(game); // Cascada sobre jugadores, mapa y casillas.
    }
}
