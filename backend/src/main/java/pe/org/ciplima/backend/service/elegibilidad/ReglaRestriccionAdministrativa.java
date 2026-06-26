package pe.org.ciplima.backend.service.elegibilidad;

import org.springframework.stereotype.Component;
import pe.org.ciplima.backend.client.dto.ColegiadoExternoDto;

import java.util.Optional;

/**
 * Regla de negocio: el personal administrativo no puede inscribirse en eventos sociales.
 */
@Component
public class ReglaRestriccionAdministrativa {

    public Optional<String> evaluar(ColegiadoExternoDto colegiado) {
        if (colegiado.esAdministrativo()) {
            return Optional.of(
                    "El personal administrativo no puede inscribirse en este evento social"
            );
        }
        return Optional.empty();
    }
}
