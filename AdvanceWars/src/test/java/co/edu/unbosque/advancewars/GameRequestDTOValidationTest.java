package co.edu.unbosque.advancewars;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import co.edu.unbosque.advancewars.dto.MoveUnitRequestDTO;
import co.edu.unbosque.advancewars.dto.PlayerSelectionRequestDTO;

/** Prueba restricciones de estructura de solicitudes antes de las reglas de servicio. */
class GameRequestDTOValidationTest {

    /** Rechaza coordenadas negativas o una ronda inexistente. */
    @Test
    void rejectsInvalidMovementRequest() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            assertFalse(validator.validate(new MoveUnitRequestDTO(-1, 2, 0, 1)).isEmpty());
            assertTrue(validator.validate(new MoveUnitRequestDTO(0, 2, 1, 1)).isEmpty());
        }
    }

    /** Rechaza la seleccion de una faccion sin comandante. */
    @Test
    void rejectsIncompletePlayerSelection() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            PlayerSelectionRequestDTO input = new PlayerSelectionRequestDTO();
            input.setFactionId(1L);
            assertFalse(factory.getValidator().validate(input).isEmpty());
        }
    }
}
