package pe.org.ciplima.backend.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import pe.org.ciplima.backend.client.dto.ColegiadoExternoDto;
import pe.org.ciplima.backend.config.AppProperties;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ColegiadosApiClientTest {

    private MockRestServiceServer mockServer;
    private ColegiadosApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:3001");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        AppProperties properties = new AppProperties(
                new AppProperties.EventoProperties(10, "Lima"),
                new AppProperties.ColegiadosApiProperties("http://localhost:3001"),
                new AppProperties.JwtProperties("secret", 3600),
                new AppProperties.MinioProperties("http://localhost:9000", "key", "secret", "bucket"),
                new AppProperties.CorsProperties("http://localhost:5173")
        );
        client = new ColegiadosApiClient(builder, properties);
    }

    @AfterEach
    void tearDown() {
        mockServer.verify();
    }

    @Test
    void buscarPorDniRetornaColegiadoCuandoExiste() {
        mockServer.expect(requestTo("http://localhost:3001/colegiados?dni=12345678"))
                .andRespond(withSuccess("""
                        [
                          {
                            "dni": "12345678",
                            "nombre": "Juan Perez",
                            "habilitado": true,
                            "es_administrativo": false,
                            "consejo_departamental": "Lima"
                          }
                        ]
                        """, MediaType.APPLICATION_JSON));

        Optional<ColegiadoExternoDto> resultado = client.buscarPorDni("12345678");

        assertTrue(resultado.isPresent());
        assertEquals("Juan Perez", resultado.get().nombre());
    }

    @Test
    void buscarPorDniRetornaVacioCuandoNoExiste() {
        mockServer.expect(requestTo("http://localhost:3001/colegiados?dni=00000000"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        Optional<ColegiadoExternoDto> resultado = client.buscarPorDni("00000000");

        assertTrue(resultado.isEmpty());
    }
}
