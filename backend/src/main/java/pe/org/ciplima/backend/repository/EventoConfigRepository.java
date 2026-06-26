package pe.org.ciplima.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.org.ciplima.backend.domain.entity.EventoConfig;

public interface EventoConfigRepository extends JpaRepository<EventoConfig, Short> {
}
