package pe.org.ciplima.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.org.ciplima.backend.domain.entity.SolicitudInscripcion;
import pe.org.ciplima.backend.domain.enums.EstadoSolicitud;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<SolicitudInscripcion, Long> {

    List<SolicitudInscripcion> findByEstadoOrderByCreatedAtAsc(EstadoSolicitud estado);

    long countByEstado(EstadoSolicitud estado);

    @Query("SELECT COUNT(s) FROM SolicitudInscripcion s")
    long countTotal();
}
