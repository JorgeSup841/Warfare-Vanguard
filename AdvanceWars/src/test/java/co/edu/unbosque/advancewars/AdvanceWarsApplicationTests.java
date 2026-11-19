package co.edu.unbosque.advancewars;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Verifica el inicio del contexto Spring usando una base de datos de prueba. */
@SpringBootTest
@ActiveProfiles("test")
class AdvanceWarsApplicationTests {

    /** Comprueba el arranque y la inyección de las dependencias. */
    @Test
    void contextLoads() {
    }
}
