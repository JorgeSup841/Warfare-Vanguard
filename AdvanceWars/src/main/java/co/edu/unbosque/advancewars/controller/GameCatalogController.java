package co.edu.unbosque.advancewars.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.advancewars.dto.FactionCatalogDTO;
import co.edu.unbosque.advancewars.dto.CommanderCatalogDTO;
import co.edu.unbosque.advancewars.dto.UnitCatalogDTO;
import co.edu.unbosque.advancewars.service.GameCatalogService;

/**
 * Expone DTO tipados de los catalogos sin devolver entidades de persistencia.
 */
@RestController
@RequestMapping("/api/catalog")
public class GameCatalogController {
	private final GameCatalogService catalog;

	/** Inyecta la capa de servicio del catalogo. */
	public GameCatalogController(GameCatalogService catalog) {
		this.catalog = catalog;
	}

	/** Devuelve las facciones disponibles. */
	@GetMapping("/factions")
	public List<FactionCatalogDTO> factions() {
		return catalog.getFactions();
	}

	/** Devuelve los comandantes y sus habilidades. */
	@GetMapping("/commanders")
	public List<CommanderCatalogDTO> commanders() {
		return catalog.getCommanders();
	}

	/** Devuelve los tipos de unidad con armas y estadisticas. */
	@GetMapping("/units")
	public List<UnitCatalogDTO> units() {
		return catalog.getUnits();
	}
}
