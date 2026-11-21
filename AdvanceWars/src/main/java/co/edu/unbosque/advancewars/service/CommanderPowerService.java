package co.edu.unbosque.advancewars.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.advancewars.dto.EndTurnRequestDTO;
import co.edu.unbosque.advancewars.dto.GameDTO;
import co.edu.unbosque.advancewars.enums.GameLogType;
import co.edu.unbosque.advancewars.enums.GameStatus;
import co.edu.unbosque.advancewars.model.Commander;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Player;
import co.edu.unbosque.advancewars.model.Unit;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.GameStateRepository;
import co.edu.unbosque.advancewars.repository.UnitRepository;

/** Valida y aplica poderes de comandantes exclusivamente desde Spring Boot. */
@Service
public class CommanderPowerService {

    private static final int HEALING_POINTS = 2;
    private final GameStateRepository games;
    private final UnitRepository units;
    private final GameEventService events;

    /** Inyecta repositorios y registro de eventos según el patrón del proyecto. */
    public CommanderPowerService(GameStateRepository games, UnitRepository units, GameEventService events) {
        this.games = games;
        this.units = units;
        this.events = events;
    }

    /** Activa el poder en la transacción de la partida, descontando su coste real. */
    @Transactional
    public GameDTO activate(Long gameId, EndTurnRequestDTO request, User user) {
        Long userId = authenticatedId(user);
        requireValidRequest(gameId, request);
        GameState game = games.findWithLockByIdAndCreatedBy_Id(gameId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));
        requireCurrentTurn(game, request);
        Player player = requireCommander(game);
        Commander commander = player.getCommander();
        String effect = commander.getPowerEffectCode();
        requireSupportedPower(effect);
        requireCharge(player, commander);

        player.spendPower(commander.getPowerCost());
        if ("HEAL_20".equals(effect)) {
            healArmy(player);
        }
        events.record(game, player, GameLogType.COMMANDER_POWER_USED,
                player.getName() + " activó " + commander.getPowerName());
        return new GameDTO(game);
    }

    /** Comprueba que la sesión corresponde a una cuenta autenticada. */
    private Long authenticatedId(User user) {
        if (user == null || user.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión");
        }
        return user.getId();
    }

    /** Rechaza solicitudes incompletas antes de tocar la partida. */
    private void requireValidRequest(Long gameId, EndTurnRequestDTO request) {
        if (gameId == null || gameId <= 0 || request == null || request.getCurrentRound() == null
                || request.getCurrentPlayerOrder() == null || request.getCurrentRound() < 1
                || request.getCurrentPlayerOrder() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica partida, ronda y jugador válidos");
        }
    }

    /** Evita gastar carga en un turno ya cambiado o terminado. */
    private void requireCurrentTurn(GameState game, EndTurnRequestDTO request) {
        if (game.getStatus() != GameStatus.IN_PROGRESS || game.getCurrentRound() != request.getCurrentRound()
                || game.getCurrentPlayerOrder() != request.getCurrentPlayerOrder()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Partida o turno desactualizado");
        }
    }

    /** Busca al jugador en turno y su comandante persistido. */
    private Player requireCommander(GameState game) {
        Player player = game.findPlayerByOrder(game.getCurrentPlayerOrder());
        if (player == null || !player.isActive() || player.getCommander() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El jugador no tiene comandante activo");
        }
        return player;
    }

    /** Solo permite poderes cuyos efectos están implementados en el motor. */
    private void requireSupportedPower(String effect) {
        List<String> implemented = List.of("HEAL_20", "MOVE_PLUS_1", "ENEMY_MOVE_MINUS_1", "RANGED_ATTACK_20");
        if (effect == null || !implemented.contains(effect)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Poder no implementado");
        }
    }

    /** Impide activar un poder por segunda vez o con carga insuficiente. */
    private void requireCharge(Player player, Commander commander) {
        if (commander.getPowerCost() <= 0 || player.isPowerActive()
                || player.getPowerCharge() < commander.getPowerCost()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Poder ya activado o carga insuficiente");
        }
    }

    /** Aplica la recuperación existente sin rebasar la vida máxima de cada unidad. */
    private void healArmy(Player player) {
        for (Unit unit : units.findAllByOwner_Id(player.getId())) {
            if (!unit.isDestroyed()) {
                unit.repair(HEALING_POINTS);
            }
        }
    }
}
