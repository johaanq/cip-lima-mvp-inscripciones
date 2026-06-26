package pe.org.ciplima.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.org.ciplima.backend.domain.entity.EventoConfig;

public interface EventoConfigRepository extends JpaRepository<EventoConfig, Short> {

    @Modifying
    @Query("""
            UPDATE EventoConfig e
            SET e.cupoOcupado = e.cupoOcupado + 1
            WHERE e.id = :id AND e.cupoOcupado < e.cupoMaximo
            """)
    int incrementarCupoSiDisponible(@Param("id") Short id);
}
