package pe.org.ciplima.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "evento_config")
public class EventoConfig {

    @Id
    private Short id = 1;

    @Column(name = "cupo_maximo", nullable = false)
    private int cupoMaximo;

    @Column(name = "cupo_ocupado", nullable = false)
    private int cupoOcupado;

    @Column(name = "sede_consejo", nullable = false, length = 100)
    private String sedeConsejo;

    protected EventoConfig() {
    }

    public EventoConfig(int cupoMaximo, int cupoOcupado, String sedeConsejo) {
        this.id = 1;
        this.cupoMaximo = cupoMaximo;
        this.cupoOcupado = cupoOcupado;
        this.sedeConsejo = sedeConsejo;
    }

    public Short getId() {
        return id;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public int getCupoOcupado() {
        return cupoOcupado;
    }

    public String getSedeConsejo() {
        return sedeConsejo;
    }

    public boolean tieneCupoDisponible() {
        return cupoOcupado < cupoMaximo;
    }

    public int getCupoDisponible() {
        return Math.max(0, cupoMaximo - cupoOcupado);
    }

    public void incrementarCupoOcupado() {
        if (!tieneCupoDisponible()) {
            throw new IllegalStateException("No hay cupo disponible para el evento");
        }
        cupoOcupado++;
    }
}
