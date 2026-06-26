package pe.org.ciplima.backend.dto;

import java.time.OffsetDateTime;

public record SolicitudPendienteResponse(
        Long id,
        String dniColegiado,
        String nombreColegiado,
        String dniMenor,
        OffsetDateTime createdAt
) {
}
