package pe.org.ciplima.backend.security;

import org.junit.jupiter.api.Test;
import pe.org.ciplima.backend.config.AppProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(new AppProperties(
            new AppProperties.EventoProperties(10, "Lima"),
            new AppProperties.ColegiadosApiProperties("http://localhost:3001"),
            new AppProperties.JwtProperties("secreto-jwt-de-prueba-con-longitud-suficiente-256bits", 3600000),
            new AppProperties.AdminProperties("admin", "admin123"),
            new AppProperties.MinioProperties("http://localhost:9000", "key", "secret", "bucket"),
            new AppProperties.CorsProperties("http://localhost:5173")
    ));

    @Test
    void generaYValidaTokenParaUsuarioAdmin() {
        String token = jwtService.generateToken("admin");

        assertEquals("admin", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, "admin"));
    }
}
