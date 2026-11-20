package co.edu.unbosque.advancewars;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.advancewars.dto.EndTurnRequestDTO;
import co.edu.unbosque.advancewars.dto.GameDTO;
import co.edu.unbosque.advancewars.enums.GameStatus;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Player;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.GameStateRepository;
import co.edu.unbosque.advancewars.repository.PropertyRepository;
import co.edu.unbosque.advancewars.repository.UnitRepository;
import co.edu.unbosque.advancewars.service.GameService;
import co.edu.unbosque.advancewars.service.MapGenerationService;
import co.edu.unbosque.advancewars.service.PropertyGenerationService;
import co.edu.unbosque.advancewars.service.UnitGenerationService;

class GameEndTurnVictoryTest {
    private GameService service;
    private GameState game;
    private User creator;
    private Player first;
    private Player second;
    private Player third;

    @BeforeEach
    void setUp() {
        GameStateRepository games = mock(GameStateRepository.class);
        PropertyRepository properties = mock(PropertyRepository.class);
        UnitRepository units = mock(UnitRepository.class);
        service = new GameService(games, mock(MapGenerationService.class),
                mock(PropertyGenerationService.class), properties,
                mock(UnitGenerationService.class), units);

        creator = new User("tester", "pass", "tester@example.com");
        ReflectionTestUtils.setField(creator, "id", 40L);
        game = new GameState(123L);
        ReflectionTestUtils.setField(game, "id", 2L);
        first = new Player("Juancho", 1, 5000);
        second = new Player("Jorge", 2, 5000);
        third = new Player("Tercero", 3, 5000);
        ReflectionTestUtils.setField(first, "id", 11L);
        ReflectionTestUtils.setField(second, "id", 12L);
        ReflectionTestUtils.setField(third, "id", 13L);
        game.addPlayer(first);
        game.addPlayer(second);
        game.addPlayer(third);
        game.startGame(1);
        when(games.findWithLockByIdAndCreatedBy_Id(2L, 40L)).thenReturn(Optional.of(game));
        when(properties.findAllByOwner_Id(11L)).thenReturn(List.of());
        when(properties.findAllByOwner_Id(13L)).thenReturn(List.of());
        when(units.findAllByOwner_Id(11L)).thenReturn(List.of());
        when(units.findAllByOwner_Id(13L)).thenReturn(List.of());
    }

    @Test
    void skipsEliminatedPlayer() {
        second.eliminate();
        GameDTO result = service.endTurn(2L, new EndTurnRequestDTO(1, 1), creator);
        assertEquals(1, result.getCurrentRound());
        assertEquals(3, result.getCurrentPlayerOrder());
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
    }

    @Test
    void wrapsRoundAfterSkippingEliminatedPlayer() {
        second.eliminate();
        game.changeCurrentPlayer(3);
        GameDTO result = service.endTurn(2L, new EndTurnRequestDTO(1, 3), creator);
        assertEquals(2, result.getCurrentRound());
        assertEquals(1, result.getCurrentPlayerOrder());
    }

    @Test
    void rejectsEndTurnAfterFinishedGame() {
        second.eliminate();
        third.eliminate();
        game.finishGame(1);
        try {
            service.endTurn(2L, new EndTurnRequestDTO(1, 1), creator);
            fail("Una partida terminada no debe avanzar de turno");
        } catch (ResponseStatusException exception) {
            assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        }
    }
}
