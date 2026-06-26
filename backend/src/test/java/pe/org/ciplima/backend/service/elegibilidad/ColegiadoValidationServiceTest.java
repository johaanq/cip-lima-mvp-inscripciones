package pe.org.ciplima.backend.service.elegibilidad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.org.ciplima.backend.client.dto.ColegiadoExternoDto;
import pe.org.ciplima.backend.config.AppProperties;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColegiadoValidationServiceTest {

    private ColegiadoValidationService service;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties(
                new AppProperties.EventoProperties(10, "Lima"),
                new AppProperties.ColegiadosApiProperties("http://localhost:3001"),
                new AppProperties.JwtProperties("secret", 3600),
                new AppProperties.AdminProperties("admin", "admin"),
                new AppProperties.MinioProperties("http://localhost:9000", "key", "secret", "bucket"),
                new AppProperties.CorsProperties("http://localhost:5173")
        );

        service = new ColegiadoValidationService(
                new ReglaColegiadoHabilitado(),
                new ReglaConsejoDepartamental(properties),
                new ReglaRestriccionAdministrativa()
        );
    }

    @Test
    void apruebaColegiadoElegibleDeLima() {
        ColegiadoExternoDto colegiado = new ColegiadoExternoDto(
                "12345678", "Juan Perez", true, false, "Lima"
        );

        ResultadoElegibilidad resultado = service.evaluar(Optional.of(colegiado));

        assertTrue(resultado.elegible());
    }

    @Test
    void rechazaColegiadoNoHabilitado() {
        ColegiadoExternoDto colegiado = new ColegiadoExternoDto(
                "11223344", "Carlos Ruiz", false, false, "Callao"
        );

        ResultadoElegibilidad resultado = service.evaluar(Optional.of(colegiado));

        assertFalse(resultado.elegible());
        assertEquals("El colegiado no se encuentra habilitado para inscribirse", resultado.motivoRechazo());
    }

    @Test
    void rechazaColegiadoAdministrativo() {
        ColegiadoExternoDto colegiado = new ColegiadoExternoDto(
                "87654321", "Maria Lopez", true, true, "Lima"
        );

        ResultadoElegibilidad resultado = service.evaluar(Optional.of(colegiado));

        assertFalse(resultado.elegible());
        assertEquals(
                "El personal administrativo no puede inscribirse en este evento social",
                resultado.motivoRechazo()
        );
    }

    @Test
    void rechazaColegiadoFueraDeLima() {
        ColegiadoExternoDto colegiado = new ColegiadoExternoDto(
                "44332211", "Ana Gomez", true, false, "Cusco"
        );

        ResultadoElegibilidad resultado = service.evaluar(Optional.of(colegiado));

        assertFalse(resultado.elegible());
        assertEquals("El colegiado no pertenece al Consejo Departamental de Lima", resultado.motivoRechazo());
    }

    @Test
    void rechazaCuandoNoExisteColegiado() {
        ResultadoElegibilidad resultado = service.evaluar(Optional.empty());

        assertFalse(resultado.elegible());
        assertEquals("No se encontró un colegiado registrado con el DNI indicado", resultado.motivoRechazo());
    }
}
