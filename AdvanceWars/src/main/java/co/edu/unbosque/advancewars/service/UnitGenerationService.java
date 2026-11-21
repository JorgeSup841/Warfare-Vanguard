package co.edu.unbosque.advancewars.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.advancewars.enums.MovementType;
import co.edu.unbosque.advancewars.enums.PropertyType;
import co.edu.unbosque.advancewars.enums.UnitCategory;
import co.edu.unbosque.advancewars.enums.UnitType;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Player;
import co.edu.unbosque.advancewars.model.Property;
import co.edu.unbosque.advancewars.model.Unit;
import co.edu.unbosque.advancewars.model.UnitDefinition;
import co.edu.unbosque.advancewars.repository.UnitDefinitionRepository;

@Service
public class UnitGenerationService {

	private final UnitDefinitionRepository definitionRepository;

	public UnitGenerationService(UnitDefinitionRepository definitionRepository) {
		this.definitionRepository = definitionRepository;
	}

	// Coloca una infanteria sobre el cuartel general de cada jugador.
	public List<Unit> generateInitialUnits(GameState game, List<Property> properties) {
		Map<Integer, Property> bases = new HashMap<>();

		for (Property property : properties) {
			if (property.getDefinition().getType() == PropertyType.HEADQUARTERS && property.getOwner() != null) {
				int order = property.getOwner().getTurnOrder();
				if (bases.putIfAbsent(order, property) != null) {
					throw new ResponseStatusException(HttpStatus.CONFLICT, "Hay varias bases para el mismo jugador");
				}
			}
		}

		// Validamos antes de crear una definicion de unidad en la base de datos.
		for (Player player : game.getPlayers()) {
			Property base = bases.get(player.getTurnOrder());
			if (base == null || base.getTile() == null || (base.getOwner().getId() != null && player.getId() != null
					&& !base.getOwner().getId().equals(player.getId()))) {
				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"Falta el cuartel general del jugador " + player.getName());
			}
		}

		UnitDefinition infantry = definitionRepository.findByUnitType(UnitType.INFANTRY)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Catalogo de unidades no inicializado"));

		List<Unit> units = new ArrayList<>();
		for (Player player : game.getPlayers()) {
			units.add(new Unit(infantry, player, bases.get(player.getTurnOrder()).getTile()));
		}
		return units;
	}
}
