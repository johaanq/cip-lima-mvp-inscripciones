package pe.org.ciplima.backend.dto;

public record AdminMetricasResponse(
        long total,
        long aprobados,
        long rechazados,
        long pendientes,
        int cupoDisponible
) {
}
