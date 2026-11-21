package co.edu.unbosque.advancewars.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import co.edu.unbosque.advancewars.dto.EndTurnRequestDTO;
import co.edu.unbosque.advancewars.dto.GameDTO;
import co.edu.unbosque.advancewars.enums.GameLogType;
import co.edu.unbosque.advancewars.enums.GameStatus;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Player;
import co.edu.unbosque.advancewars.model.Unit;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.GameStateRepository;
import co.edu.unbosque.advancewars.repository.UnitRepository;

@Service
public class CommanderPowerService {
    private final GameStateRepository games;
    private final UnitRepository units;
    private final GameEventService events;
    public CommanderPowerService(GameStateRepository games, UnitRepository units, GameEventService events) {
        this.games = games; this.units = units; this.events = events;
    }

    @Transactional
    public GameDTO activate(Long gameId, EndTurnRequestDTO request, User user) {
        if (user == null || user.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesion");
        }
        if (request == null || request.getCurrentRound() == null || request.getCurrentPlayerOrder() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica ronda y jugador actual");
        }
        GameState game = games.findWithLockByIdAndCreatedBy_Id(gameId, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));
        if (game.getStatus() != GameStatus.IN_PROGRESS || game.getCurrentRound() != request.getCurrentRound()
                || game.getCurrentPlayerOrder() != request.getCurrentPlayerOrder()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Partida o turno desactualizado");
        }
        Player player = game.findPlayerByOrder(game.getCurrentPlayerOrder());
        if (player == null || !player.isActive() || player.getCommander() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El jugador no tiene comandante activo");
        }
        if (player.isPowerActive() || player.getPowerCharge() < player.getCommander().getPowerCost()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Poder ya activado o carga insuficiente");
        }
        String effect = player.getCommander().getPowerEffectCode();
        if (!effect.equals("HEAL_20") && !effect.equals("MOVE_PLUS_1")
                && !effect.equals("ENEMY_MOVE_MINUS_1") && !effect.equals("RANGED_ATTACK_20")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Poder no implementado");
        }
        player.spendPower(player.getCommander().getPowerCost());
        if (effect.equals("HEAL_20")) {
            for (Unit unit : units.findAllByOwner_Id(player.getId())) unit.repair(2);
        }
        events.record(game, player, GameLogType.COMMANDER_POWER_USED,
            player.getName() + " activo " + player.getCommander().getPowerName());
        return new GameDTO(game);
    }
}
