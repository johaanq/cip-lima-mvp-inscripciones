package pe.org.ciplima.backend.service.elegibilidad;

import org.springframework.stereotype.Component;
import pe.org.ciplima.backend.client.dto.ColegiadoExternoDto;

import java.util.Optional;

/**
 * Regla de negocio: el colegiado debe figurar como habilitado.
 */
@Component
public class ReglaColegiadoHabilitado {

    public Optional<String> evaluar(ColegiadoExternoDto colegiado) {
        if (!colegiado.habilitado()) {
            return Optional.of("El colegiado no se encuentra habilitado para inscribirse");
        }
        return Optional.empty();
    }
}
