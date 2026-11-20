//package co.edu.unbosque.advancewars;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//import java.util.List;
//import java.util.Set;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import co.edu.unbosque.advancewars.dto.GameDTO;
//import co.edu.unbosque.advancewars.enums.GameStatus;
//import co.edu.unbosque.advancewars.enums.MovementType;
//import co.edu.unbosque.advancewars.enums.PlayerStatus;
//import co.edu.unbosque.advancewars.enums.PropertyType;
//import co.edu.unbosque.advancewars.enums.TerrainType;
//import co.edu.unbosque.advancewars.enums.UnitCategory;
//import co.edu.unbosque.advancewars.enums.UnitType;
//import co.edu.unbosque.advancewars.model.GameMap;
//import co.edu.unbosque.advancewars.model.GameState;
//import co.edu.unbosque.advancewars.model.Player;
//import co.edu.unbosque.advancewars.model.Position;
//import co.edu.unbosque.advancewars.model.Property;
//import co.edu.unbosque.advancewars.model.PropertyDefinition;
//import co.edu.unbosque.advancewars.model.Tile;
//import co.edu.unbosque.advancewars.model.Unit;
//import co.edu.unbosque.advancewars.model.UnitDefinition;
//import co.edu.unbosque.advancewars.repository.PropertyRepository;
//import co.edu.unbosque.advancewars.repository.UnitRepository;
//import co.edu.unbosque.advancewars.service.GameVictoryService;
//
//class GameVictoryServiceTest {
//
//	private UnitRepository units;
//	private PropertyRepository properties;
//	private GameVictoryService service;
//	private GameState game;
//	private Player juancho;
//	private Player jorge;
//	private GameMap map;
//
//	@BeforeEach
//	void setUp() {
//		units = mock(UnitRepository.class);
//		properties = mock(PropertyRepository.class);
//		service = new GameVictoryService(units, properties);
//
//		game = new GameState(123L);
//		ReflectionTestUtils.setField(game, "id", 2L);
//		juancho = new Player("Juancho", 1, 5000);
//		jorge = new Player("Jorge", 2, 5000);
//		ReflectionTestUtils.setField(juancho, "id", 10L);
//		ReflectionTestUtils.setField(jorge, "id", 20L);
//		game.addPlayer(juancho);
//		game.addPlayer(jorge);
//		game.startGame(1);
//
//		map = new GameMap(10, 10);
//		ReflectionTestUtils.setField(map, "id", 100L);
//		game.setGameMap(map);
//
//		when(units.findAllByOwner_Id(20L)).thenReturn(List.of());
//		when(properties.findAllByTile_GameMap_Id(100L)).thenReturn(List.of());
//	}
//
//	private Property headquarters(int x, int y, Player owner) {
//		Tile tile = new Tile(new Position(x, y), TerrainType.PLAIN);
//		PropertyDefinition definition = new PropertyDefinition(PropertyType.HEADQUARTERS, "Cuartel", 0, 4, 20,
//				Set.of());
//		return new Property(definition, tile, owner);
//	}
//
//	@Test
//	void capturingJorgeOriginalHeadquartersEndsTwoPlayerGame() {
//		Property hq = headquarters(8, 8, jorge);
//		hq.completeCapture(juancho);
//		service.checkHeadquartersCapture(game, hq, juancho);
//
//		assertEquals(PlayerStatus.ELIMINATED, jorge.getStatus());
//		assertEquals(PlayerStatus.WINNER, juancho.getStatus());
//		assertEquals(GameStatus.FINISHED, game.getStatus());
//		assertEquals(1, game.getWinnerPlayerOrder());
//		assertNotNull(game.getFinishedAt());
//		assertEquals(Integer.valueOf(1), new GameDTO(game).getWinnerPlayerOrder());
//	}
//
//	@Test
//	void armyDefeatEndsTwoPlayerGame() {
//		when(units.countByOwner_Id(20L)).thenReturn(0L);
//		service.checkArmyDefeat(game, jorge);
//
//		assertEquals(GameStatus.FINISHED, game.getStatus());
//		assertEquals(PlayerStatus.WINNER, juancho.getStatus());
//		assertEquals(PlayerStatus.ELIMINATED, jorge.getStatus());
//	}
//
//	@Test
//	void keepingOneUnitPreventsArmyDefeat() {
//		when(units.countByOwner_Id(20L)).thenReturn(1L);
//		service.checkArmyDefeat(game, jorge);
//
//		assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
//		assertTrue(jorge.isActive());
//		verify(properties, never()).save(any(Property.class));
//	}
//
//	@Test
//	void headquartersOfEliminatedPlayerDoesNotDefeatNewOwner() {
//		jorge.eliminate();
//		Property hq = headquarters(8, 8, juancho);
//		Player third = new Player("Tercero", 3, 5000);
//		ReflectionTestUtils.setField(third, "id", 30L);
//		game.addPlayer(third);
//		hq.completeCapture(third);
//		service.checkHeadquartersCapture(game, hq, third);
//
//		assertTrue(juancho.isActive());
//		assertTrue(third.isActive());
//		assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
//	}
//
//	@Test
//	void captureInThreePlayerGameEliminatesOnlyDefeated() {
//		Player third = new Player("Tercero", 3, 5000);
//		ReflectionTestUtils.setField(third, "id", 30L);
//		game.addPlayer(third);
//		Property hq = headquarters(8, 8, jorge);
//		hq.completeCapture(juancho);
//
//		service.checkHeadquartersCapture(game, hq, juancho);
//
//		assertFalse(jorge.isActive());
//		assertTrue(juancho.isActive());
//		assertTrue(third.isActive());
//		assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
//		assertNull(game.getWinnerPlayerOrder());
//	}
//
//	@Test
//	void defeatedPlayerPropertiesBecomeNeutralAndCapturesReset() {
//		Property city = new Property(new PropertyDefinition(PropertyType.CITY, "Ciudad", 1000, 3, 20, Set.of()),
//				new Tile(new Position(2, 2), TerrainType.PLAIN), jorge);
//		Property otherCapture = new Property(
//				new PropertyDefinition(PropertyType.FACTORY, "Fabrica", 1000, 3, 20, Set.of()),
//				new Tile(new Position(4, 4), TerrainType.PLAIN));
//		otherCapture.updateCaptureState(jorge, 10);
//		when(properties.findAllByTile_GameMap_Id(100L)).thenReturn(List.of(city, otherCapture));
//
//		service.checkArmyDefeat(game, jorge);
//
//		assertNull(city.getOwner());
//		assertEquals(0, otherCapture.getCaptureProgress());
//		assertNull(otherCapture.getCapturingPlayer());
//		verify(properties).save(city);
//		verify(properties).save(otherCapture);
//	}
//
//	@Test
//	void headquartersCaptureDeletesRemainingDefeatedArmy() {
//		UnitDefinition definition = new UnitDefinition(UnitType.INFANTRY, "Infanteria", UnitCategory.LAND,
//				MovementType.FOOT, 1000, 10, 3, true, 99, 0, 1, true);
//		Unit defender = new Unit(definition, jorge, new Tile(new Position(7, 8), TerrainType.PLAIN));
//		when(units.findAllByOwner_Id(20L)).thenReturn(List.of(defender));
//		Property hq = headquarters(8, 8, jorge);
//		hq.completeCapture(juancho);
//
//		service.checkHeadquartersCapture(game, hq, juancho);
//
//		verify(units).delete(defender);
//		assertEquals(GameStatus.FINISHED, game.getStatus());
//	}
//
//	@Test
//	void capturingYourOwnOriginalHeadquartersDoesNotEliminateYourself() {
//		Property hq = headquarters(1, 1, jorge);
//		hq.completeCapture(juancho);
//		service.checkHeadquartersCapture(game, hq, juancho);
//		assertTrue(juancho.isActive());
//		assertTrue(jorge.isActive());
//		assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
//	}
//}
