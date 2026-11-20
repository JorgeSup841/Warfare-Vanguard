package co.edu.unbosque.advancewars;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.advancewars.dto.AttackResultDTO;
import co.edu.unbosque.advancewars.dto.AttackUnitRequestDTO;
import co.edu.unbosque.advancewars.enums.GameStatus;
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
import co.edu.unbosque.advancewars.repository.UnitRepository;
import co.edu.unbosque.advancewars.service.UnitCombatService;

class UnitCombatServiceTest {

    private GameStateRepository games;
    private UnitRepository units;
    private UnitCombatService service;
    private GameState game;
    private User creator;
    private Unit attacker;
    private Unit target;
    private Player first;
    private Player second;
    private GameMap map;

    @BeforeEach
    void setUp() {
        games = mock(GameStateRepository.class);
        units = mock(UnitRepository.class);
        service = new UnitCombatService(games, units);

        creator = new User("tester", "pass", "test@example.com");
        ReflectionTestUtils.setField(creator, "id", 4L);

        game = new GameState(44L);
        ReflectionTestUtils.setField(game, "id", 2L);
        first = new Player("Juancho", 1, 5000);
        second = new Player("Jorge", 2, 5000);
        ReflectionTestUtils.setField(first, "id", 7L);
        ReflectionTestUtils.setField(second, "id", 8L);
        game.addPlayer(first);
        game.addPlayer(second);
        game.startGame(1);
        game.incrementRound();

        map = new GameMap(5, 5);
        long nextId = 1L;
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                Tile t = new Tile(new Position(x, y), TerrainType.PLAIN);
                ReflectionTestUtils.setField(t, "id", nextId++);
                map.addTile(t);
            }
        }
        game.setGameMap(map);

        UnitDefinition definition = new UnitDefinition(UnitType.INFANTRY, "Infanteria",
                UnitCategory.LAND, MovementType.FOOT, 1000, 10, 3, true, 99, 0, 1, true);
        attacker = new Unit(definition, first, tile(2, 1));
        target = new Unit(definition, second, tile(2, 2));
        ReflectionTestUtils.setField(attacker, "id", 15L);
        ReflectionTestUtils.setField(target, "id", 16L);

        when(games.findWithLockByIdAndCreatedBy_Id(2L, 4L)).thenReturn(Optional.of(game));
        when(units.findById(15L)).thenReturn(Optional.of(attacker));
        when(units.findById(16L)).thenReturn(Optional.of(target));
    }

    private Tile tile(int x, int y) { return map.findTileAt(x, y).orElseThrow(); }
    private AttackUnitRequestDTO request() { return new AttackUnitRequestDTO(16L, 2, 1); }

    private void assertStatus(HttpStatus status, Runnable action) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, action::run);
        assertEquals(status, ex.getStatusCode());
        verify(units, never()).save(any(Unit.class));
        verify(units, never()).delete(any(Unit.class));
    }

    @Test
    void attacksAndTargetCounterattacks() {
        AttackResultDTO result = service.attack(2L, 15L, request(), creator);
        assertEquals(4, result.getDamageDealt());
        assertEquals(2, result.getCounterattackDamage());
        assertEquals(8, result.getAttackerHealth());
        assertEquals(6, result.getTargetHealth());
        assertFalse(result.isAttackerDestroyed());
        assertFalse(result.isTargetDestroyed());
        assertTrue(attacker.isAttackedThisTurn());
        verify(units).save(attacker);
        verify(units).save(target);
        verify(units, never()).delete(any(Unit.class));
    }

    @Test
    void canAttackAfterMovingButNotAfterCapturing() {
        attacker.setMovedThisTurn(true);
        assertEquals(4, service.attack(2L, 15L, request(), creator).getDamageDealt());
    }

    @Test
    void cannotAttackTwiceSameTurn() {
        service.attack(2L, 15L, request(), creator);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.attack(2L, 15L, request(), creator));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(units, times(1)).save(target);
    }

    @Test
    void noCounterattackWhenDefenderDies() {
        target.setCurrentHealth(3);
        AttackResultDTO result = service.attack(2L, 15L, request(), creator);
        assertEquals(3, result.getDamageDealt());
        assertEquals(0, result.getCounterattackDamage());
        assertEquals(10, result.getAttackerHealth());
        assertEquals(0, result.getTargetHealth());
        assertTrue(result.isTargetDestroyed());
        verify(units).delete(target);
        verify(units).save(attacker);
        verify(units, never()).save(target);
    }

    @Test
    void counterattackCanDestroyAttacker() {
        attacker.setCurrentHealth(1);
        AttackResultDTO result = service.attack(2L, 15L, request(), creator);
        assertEquals(1, result.getCounterattackDamage());
        assertTrue(result.isAttackerDestroyed());
        verify(units).delete(attacker);
        verify(units).save(target);
        verify(units, never()).save(attacker);
    }

    @Test
    void refusesDistantAttack() {
        target.setTile(tile(4, 4));
        assertStatus(HttpStatus.CONFLICT, () -> service.attack(2L, 15L, request(), creator));
    }

    @Test
    void refusesDiagonalAttack() {
        target.setTile(tile(3, 2));
        assertStatus(HttpStatus.CONFLICT, () -> service.attack(2L, 15L, request(), creator));
    }

    @Test
    void refusesFriendlyFire() {
        target.setOwner(first);
        assertStatus(HttpStatus.CONFLICT, () -> service.attack(2L, 15L, request(), creator));
    }

    @Test
    void refusesAttackByOpponentOutsideTurn() {
        attacker.setOwner(second);
        target.setOwner(first);
        assertStatus(HttpStatus.CONFLICT, () -> service.attack(2L, 15L, request(), creator));
    }

    @Test
    void refusesAttackAfterCapture() {
        attacker.markCaptured();
        assertStatus(HttpStatus.CONFLICT, () -> service.attack(2L, 15L, request(), creator));
    }

    @Test
    void refusesStaleRound() {
        assertStatus(HttpStatus.CONFLICT, () -> service.attack(2L, 15L,
                new AttackUnitRequestDTO(16L, 1, 1), creator));
    }

    @Test
    void refusesInvalidRequest() {
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.attack(2L, 15L, null, creator));
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.attack(2L, 15L,
                new AttackUnitRequestDTO(null, 2, 1), creator));
    }

    @Test
    void refusesSelfAttack() {
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.attack(2L, 15L,
                new AttackUnitRequestDTO(15L, 2, 1), creator));
    }

    @Test
    void refusesGameNotRunning() {
        game.setStatus(GameStatus.SETUP);
        assertStatus(HttpStatus.CONFLICT, () -> service.attack(2L, 15L, request(), creator));
    }

    @Test
    void refusesUnitFromAnotherGame() {
        GameState secondGame = new GameState(999L);
        ReflectionTestUtils.setField(secondGame, "id", 99L);
        Player other = new Player("Other", 1, 5000);
        secondGame.addPlayer(other);
        target.setOwner(other);
        assertStatus(HttpStatus.NOT_FOUND, () -> service.attack(2L, 15L, request(), creator));
    }

    @Test
    void refusesAnonymousRequest() {
        assertStatus(HttpStatus.UNAUTHORIZED, () -> service.attack(2L, 15L, request(), null));
    }

    @Test
    void refusesNonInfantryCombat() {
        UnitDefinition tank = new UnitDefinition(UnitType.TANK, "Tank", UnitCategory.LAND,
                MovementType.TREAD, 5000, 10, 4, true, 70, 0, 1, false);
        target.setDefinition(tank);
        assertStatus(HttpStatus.CONFLICT, () -> service.attack(2L, 15L, request(), creator));
    }

    @Test
    void refusesMissingTarget() {
        when(units.findById(16L)).thenReturn(Optional.empty());
        assertStatus(HttpStatus.NOT_FOUND, () -> service.attack(2L, 15L, request(), creator));
    }

    @Test
    void refusesEliminatedTarget() {
        second.eliminate();
        assertStatus(HttpStatus.CONFLICT, () -> service.attack(2L, 15L, request(), creator));
    }
}
