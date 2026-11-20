package co.edu.unbosque.advancewars.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.advancewars.dto.ProduceUnitRequestDTO;
import co.edu.unbosque.advancewars.dto.UnitDTO;
import co.edu.unbosque.advancewars.enums.GameStatus;
import co.edu.unbosque.advancewars.enums.GameLogType;
import co.edu.unbosque.advancewars.enums.MovementType;
import co.edu.unbosque.advancewars.enums.PropertyType;
import co.edu.unbosque.advancewars.enums.UnitCategory;
import co.edu.unbosque.advancewars.enums.UnitType;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Player;
import co.edu.unbosque.advancewars.model.Property;
import co.edu.unbosque.advancewars.model.Unit;
import co.edu.unbosque.advancewars.model.UnitDefinition;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.GameStateRepository;
import co.edu.unbosque.advancewars.repository.PropertyRepository;
import co.edu.unbosque.advancewars.repository.UnitDefinitionRepository;
import co.edu.unbosque.advancewars.repository.UnitRepository;

/** Compra infanterias y tanques en fabricas propias. */
@Service
public class UnitProductionService {

	private final GameStateRepository gameRepository;
	private final PropertyRepository propertyRepository;
	private final UnitDefinitionRepository definitionRepository;
	private final UnitRepository unitRepository;
	private final GameEventService events;

	public UnitProductionService(GameStateRepository gameRepository, PropertyRepository propertyRepository,
			UnitDefinitionRepository definitionRepository, UnitRepository unitRepository, GameEventService events) {
		this.gameRepository = gameRepository;
		this.propertyRepository = propertyRepository;
		this.definitionRepository = definitionRepository;
		this.unitRepository = unitRepository;
		this.events = events;
	}

	@Transactional
	public UnitDTO produce(Long gameId, ProduceUnitRequestDTO request, User creator) {
		if (creator == null || creator.getId() == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesion");
		}
		if (request == null || request.getPropertyId() == null || request.getUnitType() == null
				|| request.getCurrentRound() == null || request.getCurrentPlayerOrder() == null
				|| request.getCurrentRound() < 1 || request.getCurrentPlayerOrder() < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos de produccion incompletos");
		}
		if (request.getUnitType() != UnitType.INFANTRY && request.getUnitType() != UnitType.TANK) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Por ahora solo se producen INFANTRY y TANK");
		}

		// El bloqueo de la partida evita compras simultaneas con el mismo dinero.
		Optional<GameState> foundGame = gameRepository.findWithLockByIdAndCreatedBy_Id(gameId, creator.getId());
		if (foundGame.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada");
		}
		GameState game = foundGame.get();

		if (game.getStatus() != GameStatus.IN_PROGRESS) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "La partida no esta en curso");
		}
		if (game.getCurrentRound() != request.getCurrentRound()
				|| game.getCurrentPlayerOrder() != request.getCurrentPlayerOrder()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El turno cambio; consulta la partida");
		}
		Player current = game.findPlayerByOrder(game.getCurrentPlayerOrder());
		if (current == null || !current.isActive()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "No hay un jugador activo en este turno");
		}

		Optional<Property> foundProperty = propertyRepository.findById(request.getPropertyId());
		if (foundProperty.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Propiedad no encontrada");
		}
		Property factory = foundProperty.get();

		// La fabrica debe pertenecer al mapa y al jugador del turno actual.
		if (game.getGameMap() == null || factory.getTile() == null || factory.getTile().getGameMap() == null
				|| !game.getGameMap().getId().equals(factory.getTile().getGameMap().getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Propiedad no encontrada en esta partida");
		}
		if (factory.getOwner() == null || current.getId() == null
				|| !current.getId().equals(factory.getOwner().getId())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "La fabrica no es de este jugador");
		}
		if (factory.getDefinition().getType() != PropertyType.FACTORY || !factory.isOperational()
				|| !factory.getDefinition().getProductionCategories().contains(UnitCategory.LAND)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Esta propiedad no puede producir tropas terrestres");
		}
		if (unitRepository.existsByTile_Id(factory.getTile().getId())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "La casilla de la fabrica esta ocupada");
		}

		// Reutilizamos las definiciones ya guardadas. Solo creamos TANK si hace falta.
		Optional<UnitDefinition> foundDefinition = definitionRepository.findByUnitType(request.getUnitType());
		UnitDefinition definition;
		boolean newTank = false;
		if (foundDefinition.isPresent()) {
			definition = foundDefinition.get();
		} else if (request.getUnitType() == UnitType.TANK) {
			definition = new UnitDefinition(UnitType.TANK, "Tanque", UnitCategory.LAND, MovementType.TREAD, 7000, 10, 6,
					true, 70, 0, 1, false);
			newTank = true;
		} else {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "No existe la definicion de infanteria");
		}
		if (definition.getCategory() != UnitCategory.LAND || definition.getProductionCost() <= 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Definicion de unidad no valida");
		}
		if (current.getMoney() < definition.getProductionCost()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Dinero insuficiente");
		}
		if (newTank) {
			definition = definitionRepository.save(definition);
		}

		Unit unit = new Unit(definition, current, factory.getTile());
		// La tropa nueva puede actuar a partir de su siguiente turno.
		unit.setMovedThisTurn(true);
		unit.setAttackedThisTurn(true);
		unit.setCapturedThisTurn(true);

		current.subtractMoney(definition.getProductionCost());
		Unit saved = unitRepository.save(unit);
		events.record(game, current, GameLogType.UNIT_PRODUCED,
				"Unidad " + definition.getUnitType() + " #" + saved.getId() + " producida en propiedad #"
						+ factory.getId() + " por " + definition.getProductionCost());
		return new UnitDTO(saved);
	}
}
