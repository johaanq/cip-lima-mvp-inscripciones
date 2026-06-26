package pe.org.ciplima.backend.service.elegibilidad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.org.ciplima.backend.client.dto.ColegiadoExternoDto;
import pe.org.ciplima.backend.config.AppProperties;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReglaConsejoDepartamentalTest {

    private ReglaConsejoDepartamental regla;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties(
                new AppProperties.EventoProperties(10, "Lima"),
                new AppProperties.ColegiadosApiProperties("http://localhost:3001"),
                new AppProperties.JwtProperties("secret", 3600),
                new AppProperties.MinioProperties("http://localhost:9000", "key", "secret", "bucket"),
                new AppProperties.CorsProperties("http://localhost:5173")
        );
        regla = new ReglaConsejoDepartamental(properties);
    }

    @Test
    void apruebaCuandoColegiadoPerteneceALima() {
        ColegiadoExternoDto colegiado = new ColegiadoExternoDto(
                "12345678", "Juan Perez", true, false, "Lima"
        );

        Optional<String> motivo = regla.evaluar(colegiado);

        assertTrue(motivo.isEmpty());
    }

    @Test
    void rechazaCuandoColegiadoNoPerteneceALima() {
        ColegiadoExternoDto colegiado = new ColegiadoExternoDto(
                "44332211", "Ana Gomez", true, false, "Cusco"
        );

        Optional<String> motivo = regla.evaluar(colegiado);

        assertTrue(motivo.isPresent());
        assertEquals("El colegiado no pertenece al Consejo Departamental de Lima", motivo.get());
    }
}
