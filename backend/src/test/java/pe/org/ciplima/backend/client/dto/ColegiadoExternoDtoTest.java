package pe.org.ciplima.backend.client.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColegiadoExternoDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializaColegiadoDesdeJsonMock() throws Exception {
        String json = """
                {
                  "dni": "12345678",
                  "nombre": "Juan Perez",
                  "habilitado": true,
                  "es_administrativo": false,
                  "consejo_departamental": "Lima"
                }
                """;

        ColegiadoExternoDto colegiado = objectMapper.readValue(json, ColegiadoExternoDto.class);

        assertEquals("12345678", colegiado.dni());
        assertEquals("Juan Perez", colegiado.nombre());
        assertTrue(colegiado.habilitado());
        assertFalse(colegiado.esAdministrativo());
        assertEquals("Lima", colegiado.consejoDepartamental());
    }
}
