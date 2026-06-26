package pe.org.ciplima.backend.service.elegibilidad;

import org.springframework.stereotype.Service;
import pe.org.ciplima.backend.client.dto.ColegiadoExternoDto;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Orquesta las reglas de elegibilidad del colegiado según la API externa.
 */
@Service
public class ColegiadoValidationService {

    private final List<Function<ColegiadoExternoDto, Optional<String>>> reglas;

    public ColegiadoValidationService(
            ReglaColegiadoHabilitado reglaHabilitado,
            ReglaConsejoDepartamental reglaConsejo,
            ReglaRestriccionAdministrativa reglaAdministrativa
    ) {
        this.reglas = List.of(
                reglaHabilitado::evaluar,
                reglaConsejo::evaluar,
                reglaAdministrativa::evaluar
        );
    }

    public ResultadoElegibilidad evaluar(Optional<ColegiadoExternoDto> colegiado) {
        if (colegiado.isEmpty()) {
            return ResultadoElegibilidad.rechazado("No se encontró un colegiado registrado con el DNI indicado");
        }

        for (Function<ColegiadoExternoDto, Optional<String>> regla : reglas) {
            Optional<String> motivo = regla.apply(colegiado.get());
            if (motivo.isPresent()) {
                return ResultadoElegibilidad.rechazado(motivo.get());
            }
        }

        return ResultadoElegibilidad.aprobado();
    }
}
