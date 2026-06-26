package pe.org.ciplima.backend.service.elegibilidad;

import org.junit.jupiter.api.Test;
import pe.org.ciplima.backend.client.dto.ColegiadoExternoDto;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReglaRestriccionAdministrativaTest {

    private final ReglaRestriccionAdministrativa regla = new ReglaRestriccionAdministrativa();

    @Test
    void apruebaCuandoColegiadoNoEsAdministrativo() {
        ColegiadoExternoDto colegiado = new ColegiadoExternoDto(
                "12345678", "Juan Perez", true, false, "Lima"
        );

        Optional<String> motivo = regla.evaluar(colegiado);

        assertTrue(motivo.isEmpty());
    }

    @Test
    void rechazaCuandoColegiadoEsAdministrativo() {
        ColegiadoExternoDto colegiado = new ColegiadoExternoDto(
                "87654321", "Maria Lopez", true, true, "Lima"
        );

        Optional<String> motivo = regla.evaluar(colegiado);

        assertTrue(motivo.isPresent());
        assertEquals(
                "El personal administrativo no puede inscribirse en este evento social",
                motivo.get()
        );
    }
}
