package pe.org.ciplima.backend.exception;

public class AforoCompletoException extends RuntimeException {

    public AforoCompletoException() {
        super("El evento ha alcanzado su aforo máximo. No se aceptan nuevas inscripciones");
    }
}
