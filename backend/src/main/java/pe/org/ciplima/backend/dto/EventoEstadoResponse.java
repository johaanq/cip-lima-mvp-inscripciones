package pe.org.ciplima.backend.dto;

public record EventoEstadoResponse(
        int cupoMaximo,
        int cupoOcupado,
        int cupoDisponible,
        boolean inscripcionesAbiertas,
        String sedeConsejo
) {
}
