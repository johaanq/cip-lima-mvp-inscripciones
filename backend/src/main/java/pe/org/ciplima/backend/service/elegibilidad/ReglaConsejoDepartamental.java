package pe.org.ciplima.backend.service.elegibilidad;

import org.springframework.stereotype.Component;
import pe.org.ciplima.backend.client.dto.ColegiadoExternoDto;
import pe.org.ciplima.backend.config.AppProperties;

import java.util.Optional;

/**
 * Regla de negocio: el colegiado debe pertenecer al consejo departamental de la sede del evento.
 */
@Component
public class ReglaConsejoDepartamental {

    private final String sedeConsejo;

    public ReglaConsejoDepartamental(AppProperties appProperties) {
        this.sedeConsejo = appProperties.evento().sedeConsejo();
    }

    public Optional<String> evaluar(ColegiadoExternoDto colegiado) {
        if (!sedeConsejo.equalsIgnoreCase(colegiado.consejoDepartamental())) {
            return Optional.of(
                    "El colegiado no pertenece al Consejo Departamental de " + sedeConsejo
            );
        }
        return Optional.empty();
    }
}
