//package co.edu.unbosque.advancewars;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//import java.util.Optional;
//import java.util.Set;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.http.HttpStatus;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.springframework.web.server.ResponseStatusException;
//
//import co.edu.unbosque.advancewars.dto.CaptureResultDTO;
//import co.edu.unbosque.advancewars.dto.EndTurnRequestDTO;
//import co.edu.unbosque.advancewars.dto.TileDTO;
//import co.edu.unbosque.advancewars.enums.GameStatus;
//import co.edu.unbosque.advancewars.enums.MovementType;
//import co.edu.unbosque.advancewars.enums.PropertyType;
//import co.edu.unbosque.advancewars.enums.TerrainType;
//import co.edu.unbosque.advancewars.enums.UnitCategory;
//import co.edu.unbosque.advancewars.enums.UnitType;
//import co.edu.unbosque.advancewars.model.GameState;
//import co.edu.unbosque.advancewars.model.Player;
//import co.edu.unbosque.advancewars.model.Position;
//import co.edu.unbosque.advancewars.model.Propertay;
//import co.edu.unbosque.advancewars.model.PropertyDefinition;
//import co.edu.unbosque.advancewars.model.Tile;
//import co.edu.unbosque.advancewars.model.Unit;
//import co.edu.unbosque.advancewars.model.UnitDefinition;
//import co.edu.unbosque.advancewars.model.User;
//import co.edu.unbosque.advancewars.repository.GameStateRepository;
//import co.edu.unbosque.advancewars.repository.PropertyRepository;
//import co.edu.unbosque.advancewars.repository.UnitRepository;
//import co.edu.unbosque.advancewars.service.UnitCaptureService;
//import co.edu.unbosque.advancewars.service.GameVictoryService;
//
//class UnitCaptureServiceTest {
//    private GameStateRepository games;
//    private UnitRepository units;
//    private PropertyRepository properties;
//    private UnitCaptureService service;
//    private GameVictoryService victoryService;
//    private GameState game;
//    private Player first;
//    private Player second;
//    private User creator;
//    private Unit infantry;
//    private Property property;
//    private Tile tile;
//
//    @BeforeEach
//    void setUp() {
//        games = mock(GameStateRepository.class);
//        units = mock(UnitRepository.class);
//        properties = mock(PropertyRepository.class);
//        victoryService = mock(GameVictoryService.class);
//        service = new UnitCaptureService(games, units, properties, victoryService);
//
//        creator = new User("tester", "pass", "test@example.com");
//        ReflectionTestUtils.setField(creator, "id", 4L);
//        game = new GameState(10L);
//        ReflectionTestUtils.setField(game, "id", 2L);
//        first = new Player("Juancho", 1, 5000);
//        second = new Player("Jorge", 2, 5000);
//        ReflectionTestUtils.setField(first, "id", 7L);
//        ReflectionTestUtils.setField(second, "id", 8L);
//        game.addPlayer(first);
//        game.addPlayer(second);
//        game.startGame(1);
//
//        tile = new Tile(new Position(2, 1), TerrainType.PLAIN);
//        ReflectionTestUtils.setField(tile, "id", 11L);
//        PropertyDefinition definition = new PropertyDefinition(PropertyType.CITY,
//                "CITY", 1000, 3, 20, Set.of());
//        property = new Property(definition, tile);
//        ReflectionTestUtils.setField(property, "id", 12L);
//
//        UnitDefinition unitDef = new UnitDefinition(UnitType.INFANTRY, "Infanteria",
//                UnitCategory.LAND, MovementType.FOOT, 1000, 10, 3, true, 99, 0, 1, true);
//        infantry = new Unit(unitDef, first, tile);
//        ReflectionTestUtils.setField(infantry, "id", 15L);
//
//        when(games.findWithLockByIdAndCreatedBy_Id(2L, 4L)).thenReturn(Optional.of(game));
//        when(units.findById(15L)).thenReturn(Optional.of(infantry));
//        when(properties.findByTile_Id(11L)).thenReturn(Optional.of(property));
//    }
//
//    private EndTurnRequestDTO turn() {
//        return new EndTurnRequestDTO(game.getCurrentRound(), game.getCurrentPlayerOrder());
//    }
//
//    private CaptureResultDTO capture() {
//        return service.capture(2L, 15L, turn(), creator);
//    }
//
//    private void rejects(HttpStatus status, Runnable call) {
//        ResponseStatusException e = assertThrows(ResponseStatusException.class, call::run);
//        assertEquals(status, e.getStatusCode());
//        verify(properties, never()).save(any(Property.class));
//    }
//
//    @Test
//    void firstCaptureAddsTenPoints() {
//        CaptureResultDTO result = capture();
//        assertFalse(result.isCaptured());
//        assertEquals(10, result.getCaptureProgress());
//        assertEquals(20, result.getRequiredProgress());
//        assertNull(result.getOwnerOrder());
//        assertEquals(1, result.getCapturingPlayerOrder());
//        assertTrue(infantry.isCapturedThisTurn());
//        verify(properties).save(property);
//        verify(units).save(infantry);
//    }
//
//    @Test
//    void completesCaptureOnSecondOwnTurn() {
//        capture();
//        infantry.resetTurnActions();
//        game.incrementRound();
//        CaptureResultDTO result = capture();
//        assertTrue(result.isCaptured());
//        assertEquals(1, result.getOwnerOrder());
//        assertEquals(0, result.getCaptureProgress());
//        assertNull(result.getCapturingPlayerOrder());
//        assertSame(first, property.getOwner());
//        assertEquals(0, property.getCaptureProgress());
//        verify(properties, times(2)).save(property);
//    }
//
//    @Test
//    void damagedInfantryCapturesSlower() {
//        infantry.setCurrentHealth(4);
//        assertEquals(4, capture().getCaptureProgress());
//    }
//
//    @Test
//    void resetsOtherPlayersPreviousProgress() {
//        property.updateCaptureState(second, 15);
//        assertEquals(10, capture().getCaptureProgress());
//    }
//
//    @Test
//    void canCaptureAfterMovement() {
//        infantry.setMovedThisTurn(true);
//        assertEquals(10, capture().getCaptureProgress());
//    }
//
//    @Test
//    void cannotCaptureTwiceInSameTurn() {
//        capture();
//        assertThrows(ResponseStatusException.class, this::capture);
//        verify(properties, times(1)).save(property);
//    }
//
//    @Test
//    void cannotCaptureAfterAttack() {
//        infantry.markAttacked();
//        rejects(HttpStatus.CONFLICT, this::capture);
//    }
//
//    @Test
//    void cannotCaptureOwnProperty() {
//        property.assignOwner(first);
//        rejects(HttpStatus.CONFLICT, this::capture);
//    }
//
//    @Test
//    void cannotCaptureWithoutProperty() {
//        when(properties.findByTile_Id(11L)).thenReturn(Optional.empty());
//        rejects(HttpStatus.CONFLICT, this::capture);
//    }
//
//    @Test
//    void cannotCaptureInNonOperationalBuilding() {
//        property.setOperational(false);
//        rejects(HttpStatus.CONFLICT, this::capture);
//    }
//
//    @Test
//    void cannotCaptureOutsideTurn() {
//        game.changeCurrentPlayer(2);
//        rejects(HttpStatus.CONFLICT, () -> service.capture(2L, 15L, turn(), creator));
//    }
//
//    @Test
//    void cannotCaptureWithStaleRound() {
//        rejects(HttpStatus.CONFLICT, () -> service.capture(2L, 15L,
//                new EndTurnRequestDTO(9, 1), creator));
//    }
//
//    @Test
//    void cannotCaptureIfGameNotStarted() {
//        game.setStatus(GameStatus.SETUP);
//        rejects(HttpStatus.CONFLICT, this::capture);
//    }
//
//    @Test
//    void cannotCaptureOtherGamesUnit() {
//        GameState otherGame = new GameState(11L);
//        Player other = new Player("Other", 1, 5000);
//        otherGame.addPlayer(other);
//        infantry.setOwner(other);
//        rejects(HttpStatus.NOT_FOUND, this::capture);
//    }
//
//    @Test
//    void cannotCaptureWithNonInfantry() {
//        UnitDefinition tank = new UnitDefinition(UnitType.TANK, "Tank",
//                UnitCategory.LAND, MovementType.TREAD, 7000, 10, 6, true, 70, 0, 1, true);
//        infantry.setDefinition(tank);
//        rejects(HttpStatus.CONFLICT, this::capture);
//    }
//
//    @Test
//    void rejectsMissingInput() {
//        rejects(HttpStatus.BAD_REQUEST, () -> service.capture(2L, 15L, null, creator));
//        rejects(HttpStatus.BAD_REQUEST, () -> service.capture(2L, 15L,
//                new EndTurnRequestDTO(1, null), creator));
//    }
//
//    @Test
//    void rejectsAnonymousUser() {
//        rejects(HttpStatus.UNAUTHORIZED, () -> service.capture(2L, 15L, turn(), null));
//    }
//
//    @Test
//    void mapTileExposesCaptureProgress() {
//        property.updateCaptureState(first, 7);
//        TileDTO result = new TileDTO(tile, property);
//        assertEquals(7, result.getCaptureProgress());
//        assertEquals(1, result.getCapturingPlayerOrder());
//        assertNull(result.getOwnerOrder());
//    }
//    @Test
//    void completedHeadquartersCaptureChecksDefeat() {
//        PropertyDefinition hqDefinition = new PropertyDefinition(PropertyType.HEADQUARTERS,
//                "HEADQUARTERS", 0, 4, 20, Set.of());
//        property.setDefinition(hqDefinition);
//        property.assignOwner(second);
//        property.updateCaptureState(first, 10);
//        capture();
//        verify(victoryService).checkHeadquartersCapture(game, property, first);
//    }
//
//    @Test
//    void completedCityCaptureDoesNotCheckVictory() {
//        property.updateCaptureState(first, 10);
//        capture();
//        verify(victoryService, never()).checkHeadquartersCapture(game, property, first);
//    }
//
//}
