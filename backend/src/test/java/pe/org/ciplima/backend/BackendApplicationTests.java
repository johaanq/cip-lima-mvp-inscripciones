package pe.org.ciplima.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pe.org.ciplima.backend.repository.AdminUsuarioRepository;
import pe.org.ciplima.backend.repository.EventoConfigRepository;
import pe.org.ciplima.backend.repository.SolicitudRepository;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class
})
class BackendApplicationTests {

    @MockBean
    private AdminUsuarioRepository adminUsuarioRepository;

    @MockBean
    private EventoConfigRepository eventoConfigRepository;

    @MockBean
    private SolicitudRepository solicitudRepository;

    @MockBean
    private S3Client s3Client;

    @Test
    void contextLoads() {
    }

}
