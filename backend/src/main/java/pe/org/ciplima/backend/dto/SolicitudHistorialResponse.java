package pe.org.ciplima.backend.dto;

import pe.org.ciplima.backend.domain.enums.OrigenRechazo;

import java.time.OffsetDateTime;

public record SolicitudHistorialResponse(
        Long id,
        String dniColegiado,
        String nombreColegiado,
        String dniMenor,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String motivoRechazo,
        OrigenRechazo origenRechazo
) {
}
