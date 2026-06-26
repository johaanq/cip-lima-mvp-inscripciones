package pe.org.ciplima.backend.service.elegibilidad;

import org.junit.jupiter.api.Test;
import pe.org.ciplima.backend.client.dto.ColegiadoExternoDto;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReglaColegiadoHabilitadoTest {

    private final ReglaColegiadoHabilitado regla = new ReglaColegiadoHabilitado();

    @Test
    void apruebaCuandoColegiadoEstaHabilitado() {
        ColegiadoExternoDto colegiado = new ColegiadoExternoDto(
                "12345678", "Juan Perez", true, false, "Lima"
        );

        Optional<String> motivo = regla.evaluar(colegiado);

        assertTrue(motivo.isEmpty());
    }

    @Test
    void rechazaCuandoColegiadoNoEstaHabilitado() {
        ColegiadoExternoDto colegiado = new ColegiadoExternoDto(
                "11223344", "Carlos Ruiz", false, false, "Callao"
        );

        Optional<String> motivo = regla.evaluar(colegiado);

        assertTrue(motivo.isPresent());
        assertEquals("El colegiado no se encuentra habilitado para inscribirse", motivo.get());
    }
}
