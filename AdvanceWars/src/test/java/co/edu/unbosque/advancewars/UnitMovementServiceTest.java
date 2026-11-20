package co.edu.unbosque.advancewars;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.advancewars.dto.MoveUnitRequestDTO;
import co.edu.unbosque.advancewars.dto.ReachableTileDTO;
import co.edu.unbosque.advancewars.dto.UnitDTO;
import co.edu.unbosque.advancewars.enums.MovementType;
import co.edu.unbosque.advancewars.enums.TerrainType;
import co.edu.unbosque.advancewars.enums.UnitCategory;
import co.edu.unbosque.advancewars.enums.UnitType;
import co.edu.unbosque.advancewars.model.GameMap;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Player;
import co.edu.unbosque.advancewars.model.Position;
import co.edu.unbosque.advancewars.model.Tile;
import co.edu.unbosque.advancewars.model.Unit;
import co.edu.unbosque.advancewars.model.UnitDefinition;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.GameStateRepository;
import co.edu.unbosque.advancewars.repository.PropertyRepository;
import co.edu.unbosque.advancewars.repository.UnitRepository;
import co.edu.unbosque.advancewars.service.UnitMovementService;

class UnitMovementServiceTest {

    private GameStateRepository games;
    private UnitRepository units;
    private UnitMovementService service;
    private GameState game;
    private User creator;
    private GameMap map;
    private Unit infantry;
    private UnitDefinition infantryDef;
    private List<Unit> allUnits;

    @BeforeEach
    void setUp() {
        games = mock(GameStateRepository.class);
        units = mock(UnitRepository.class);
        service = new UnitMovementService(games, units, mock(PropertyRepository.class));

        creator = new User("tester", "pass", "test@example.com");
        ReflectionTestUtils.setField(creator, "id", 4L);

        game = new GameState(676767L);
        ReflectionTestUtils.setField(game, "id", 2L);
        Player juancho = new Player("Juancho", 1, 5000);
        ReflectionTestUtils.setField(juancho, "id", 7L);
        game.addPlayer(juancho);
        Player jorge = new Player("Jorge", 2, 5000);
        ReflectionTestUtils.setField(jorge, "id", 8L);
        game.addPlayer(jorge);
        game.startGame(1);
        game.incrementRound(); // Simula partida del usuario en ronda 2.

        map = new GameMap(5, 5);
        long tileId = 1;
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                Tile tile = new Tile(new Position(x, y), TerrainType.PLAIN);
                ReflectionTestUtils.setField(tile, "id", tileId++);
                map.addTile(tile);
            }
        }
        game.setGameMap(map);

        infantryDef = new UnitDefinition(UnitType.INFANTRY, "Infanteria", UnitCategory.LAND,
                MovementType.FOOT, 1000, 10, 3, true, 99, 0, 1, true);
        infantry = new Unit(infantryDef, juancho, map.findTileAt(1, 1).orElseThrow());
        ReflectionTestUtils.setField(infantry, "id", 15L);
        allUnits = new ArrayList<>();
        allUnits.add(infantry);

        when(games.findWithLockByIdAndCreatedBy_Id(2L, 4L)).thenReturn(Optional.of(game));
        when(games.findByIdAndCreatedBy_Id(2L, 4L)).thenReturn(Optional.of(game));
        when(units.findById(15L)).thenReturn(Optional.of(infantry));
        when(units.findAllByOwner_GameState_Id(2L)).thenAnswer(inv -> allUnits);
    }

    private MoveUnitRequestDTO request(int x, int y) {
        return new MoveUnitRequestDTO(x, y, game.getCurrentRound(), game.getCurrentPlayerOrder());
    }

    private void terrain(int x, int y, TerrainType type) {
        map.findTileAt(x, y).orElseThrow().setTerrainType(type);
    }

    private void assertStatus(HttpStatus status, Runnable action) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, action::run);
        assertEquals(status, ex.getStatusCode());
        verify(units, never()).save(any(Unit.class));
    }

    @Test
    void movesOneTileAndConsumesOneFuel() {
        UnitDTO result = service.move(2L, 15L, request(2, 1), creator);
        assertEquals(2, result.getX());
        assertEquals(1, result.getY());
        assertEquals(98, result.getFuel());
        assertTrue(result.isMovedThisTurn());
        verify(units).save(infantry);
    }

    @Test
    void canMoveSeveralConnectedTiles() {
        UnitDTO result = service.move(2L, 15L, request(3, 1), creator);
        assertEquals(3, result.getX());
        assertEquals(97, result.getFuel());
    }

    @Test
    void forestAndMountainCostExtraPoints() {
        terrain(2, 1, TerrainType.FOREST);
        UnitDTO result = service.move(2L, 15L, request(2, 1), creator);
        assertEquals(97, result.getFuel());
    }

    @Test
    void mountainCostsTwoPointsForInfantry() {
        terrain(2, 1, TerrainType.MOUNTAIN);
        UnitDTO result = service.move(2L, 15L, request(2, 1), creator);
        assertEquals(97, result.getFuel());
    }

    @Test
    void canMoveAgainWhenItsActionsAreResetNextRound() {
        service.move(2L, 15L, request(2, 1), creator);
        infantry.resetTurnActions();
        game.incrementRound();
        UnitDTO result = service.move(2L, 15L, request(3, 1), creator);
        assertEquals(3, result.getX());
        assertEquals(97, result.getFuel());
        verify(units, times(2)).save(infantry);
    }

    @Test
    void cannotCrossWater() {
        terrain(2, 1, TerrainType.WATER);
        assertStatus(HttpStatus.CONFLICT, () -> service.move(2L, 15L, request(2, 1), creator));
    }

    @Test
    void cannotMoveFartherThanThreeMovementPoints() {
        assertStatus(HttpStatus.CONFLICT, () -> service.move(2L, 15L, request(4, 4), creator));
    }

    @Test
    void cannotMoveToOccupiedTile() {
        Player jorge = game.findPlayerByOrder(2);
        Unit other = new Unit(infantryDef, jorge, map.findTileAt(2, 1).orElseThrow());
        ReflectionTestUtils.setField(other, "id", 99L);
        allUnits.add(other);
        assertStatus(HttpStatus.CONFLICT, () -> service.move(2L, 15L, request(2, 1), creator));
    }

    @Test
    void cannotPassThroughOccupiedTileWhenDetourTooLong() {
        Player jorge = game.findPlayerByOrder(2);
        Unit other = new Unit(infantryDef, jorge, map.findTileAt(2, 1).orElseThrow());
        ReflectionTestUtils.setField(other, "id", 99L);
        allUnits.add(other);
        assertStatus(HttpStatus.CONFLICT, () -> service.move(2L, 15L, request(3, 1), creator));
    }

    @Test
    void refusesSecondMovementInSameTurn() {
        service.move(2L, 15L, request(2, 1), creator);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.move(2L, 15L, request(3, 1), creator));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(units, times(1)).save(infantry);
    }

    @Test
    void refusesStaleTurn() {
        assertStatus(HttpStatus.CONFLICT, () -> service.move(2L, 15L,
                new MoveUnitRequestDTO(2, 1, 1, 1), creator));
    }

    @Test
    void refusesOpponentUnitOutsideOpponentTurn() {
        infantry.setOwner(game.findPlayerByOrder(2));
        assertStatus(HttpStatus.CONFLICT, () -> service.move(2L, 15L, request(2, 1), creator));
    }

    @Test
    void refusesInsufficientFuel() {
        infantry.setCurrentFuel(0);
        assertStatus(HttpStatus.CONFLICT, () -> service.move(2L, 15L, request(2, 1), creator));
    }

    @Test
    void refusesOutOfBoundsDestination() {
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.move(2L, 15L, request(-1, 1), creator));
    }

    @Test
    void refusesMissingPayload() {
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.move(2L, 15L, null, creator));
    }

    @Test
    void refusesUnitFromAnotherGame() {
        GameState otherGame = new GameState(10L);
        ReflectionTestUtils.setField(otherGame, "id", 5L);
        Player otherOwner = new Player("otro", 1, 5000);
        otherGame.addPlayer(otherOwner);
        infantry.setOwner(otherOwner);
        assertStatus(HttpStatus.NOT_FOUND, () -> service.move(2L, 15L, request(2, 1), creator));
    }

    @Test
    void reachablePreviewIncludesValidTilesButNotOrigin() {
        List<ReachableTileDTO> cells = service.reachableTiles(2L, 15L, creator);
        assertTrue(cells.stream().anyMatch(c -> c.x() == 2 && c.y() == 1 && c.cost() == 1));
        assertFalse(cells.stream().anyMatch(c -> c.x() == 1 && c.y() == 1));
        assertFalse(cells.stream().anyMatch(c -> c.x() == 4 && c.y() == 4));
        verify(units, never()).save(any(Unit.class));
    }

    @Test
    void reachablePreviewRespectsWaterAndOtherUnits() {
        terrain(2, 1, TerrainType.WATER);
        Unit other = new Unit(infantryDef, game.findPlayerByOrder(2), map.findTileAt(1, 2).orElseThrow());
        ReflectionTestUtils.setField(other, "id", 99L);
        allUnits.add(other);
        List<ReachableTileDTO> cells = service.reachableTiles(2L, 15L, creator);
        assertFalse(cells.stream().anyMatch(c -> c.x() == 2 && c.y() == 1));
        assertFalse(cells.stream().anyMatch(c -> c.x() == 1 && c.y() == 2));
    }

    @Test
    void reachablePreviewRespectsFuelAndTurn() {
        infantry.setCurrentFuel(1);
        List<ReachableTileDTO> cells = service.reachableTiles(2L, 15L, creator);
        assertTrue(cells.stream().allMatch(c -> c.cost() <= 1));
        infantry.setOwner(game.findPlayerByOrder(2));
        assertTrue(service.reachableTiles(2L, 15L, creator).isEmpty());
    }

    @Test
    void reachablePreviewRequiresOwnGameAndLoggedInUser() {
        ResponseStatusException unauthorized = assertThrows(ResponseStatusException.class,
                () -> service.reachableTiles(2L, 15L, null));
        assertEquals(HttpStatus.UNAUTHORIZED, unauthorized.getStatusCode());
        ResponseStatusException missing = assertThrows(ResponseStatusException.class,
                () -> service.reachableTiles(10L, 15L, creator));
        assertEquals(HttpStatus.NOT_FOUND, missing.getStatusCode());
    }
}