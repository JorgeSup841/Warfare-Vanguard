package co.edu.unbosque.advancewars.service;

import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.unbosque.advancewars.enums.PropertyType;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Player;
import co.edu.unbosque.advancewars.model.Property;
import co.edu.unbosque.advancewars.model.Unit;
import co.edu.unbosque.advancewars.repository.PropertyRepository;
import co.edu.unbosque.advancewars.repository.UnitRepository;

/** Reglas sencillas para eliminar jugadores y declarar al ganador. */
@Service
public class GameVictoryService {

    private final UnitRepository unitRepository;
    private final PropertyRepository propertyRepository;

    public GameVictoryService(UnitRepository unitRepository, PropertyRepository propertyRepository) {
        this.unitRepository = unitRepository;
        this.propertyRepository = propertyRepository;
    }

    /** El jugador pierde cuando destruyen su ultima unidad. */
    public void checkArmyDefeat(GameState game, Player player) {
        if (player != null && player.isActive() && unitRepository.countByOwner_Id(player.getId()) == 0) {
            eliminate(game, player);
        }
    }

    /** Un cuartel capturado elimina a su dueno ORIGINAL, no al dueno temporal. */
    public void checkHeadquartersCapture(GameState game, Property headquarters, Player attacker) {
        if (headquarters.getDefinition().getType() != PropertyType.HEADQUARTERS) {
            return;
        }
        int x = headquarters.getTile().getPosition().getX();
        int y = headquarters.getTile().getPosition().getY();
        int width = game.getGameMap().getWidth();
        int height = game.getGameMap().getHeight();
        int originalOrder = 0;

        // Estas posiciones vienen de PropertyGenerationService.
        if (x == 1 && y == 1) {
            originalOrder = 1;
        } else if (x == width - 2 && y == height - 2) {
            originalOrder = 2;
        } else if (x == width - 2 && y == 1) {
            originalOrder = 3;
        } else if (x == 1 && y == height - 2) {
            originalOrder = 4;
        }

        Player originalOwner = game.findPlayerByOrder(originalOrder);
        if (originalOwner != null && originalOwner.isActive()
                && originalOwner.getTurnOrder() != attacker.getTurnOrder()) {
            eliminate(game, originalOwner);
        }
    }

    private void eliminate(GameState game, Player defeated) {
        defeated.eliminate();

        // Sus unidades desaparecen y dejan las casillas disponibles.
        List<Unit> units = unitRepository.findAllByOwner_Id(defeated.getId());
        for (Unit unit : units) {
            unitRepository.delete(unit);
        }

        // Sus edificios quedan neutrales; se cancelan sus capturas pendientes.
        List<Property> properties = propertyRepository.findAllByTile_GameMap_Id(game.getGameMap().getId());
        for (Property property : properties) {
            boolean changed = false;
            if (property.getOwner() != null && defeated.getId().equals(property.getOwner().getId())) {
                property.assignOwner(null);
                changed = true;
            }
            if (property.getCapturingPlayer() != null
                    && defeated.getId().equals(property.getCapturingPlayer().getId())) {
                property.resetCaptureProgress();
                changed = true;
            }
            if (changed) {
                propertyRepository.save(property);
            }
        }

        // En partidas de 3 o 4 jugadores pueden quedar varios participantes.
        Player winner = null;
        int activeCount = 0;
        for (Player player : game.getPlayers()) {
            if (player.isActive()) {
                winner = player;
                activeCount++;
            }
        }
        if (activeCount == 1) {
            game.finishGame(winner.getTurnOrder());
        }
    }
}
