package co.edu.unbosque.advancewars.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Comprueba que las listas del DTO no puedan alterarse desde el exterior. */
class CreateGameDTODefensiveCopyTest {

    /** La entrada y la salida mantienen copias independientes de los nombres. */
    @Test
    void shouldCopyPlayerNamesOnReadAndWrite() {
        List<String> names = new ArrayList<>(List.of("Ana", "Luis"));
        CreateGameDTO request = new CreateGameDTO();
        request.setPlayerNames(names);
        names.set(0, "Otro");
        assertEquals("Ana", request.getPlayerNames().get(0));
        List<String> readNames = request.getPlayerNames();
        readNames.set(1, "Reemplazo");
        assertEquals("Luis", request.getPlayerNames().get(1));
    }
}
