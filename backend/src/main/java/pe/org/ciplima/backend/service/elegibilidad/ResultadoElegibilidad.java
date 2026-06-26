package pe.org.ciplima.backend.service.elegibilidad;

/**
 * Resultado de evaluar la elegibilidad de un colegiado para el evento.
 */
public record ResultadoElegibilidad(boolean elegible, String motivoRechazo) {

    public static ResultadoElegibilidad aprobado() {
        return new ResultadoElegibilidad(true, null);
    }

    public static ResultadoElegibilidad rechazado(String motivo) {
        return new ResultadoElegibilidad(false, motivo);
    }
}
