package pe.org.ciplima.backend.dto;

import pe.org.ciplima.backend.domain.enums.EstadoSolicitud;

public record InscripcionResponse(
        Long id,
        EstadoSolicitud estado,
        String motivoRechazo,
        String mensaje
) {
}
