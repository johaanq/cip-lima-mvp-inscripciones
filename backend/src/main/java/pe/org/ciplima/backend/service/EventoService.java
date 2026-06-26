package pe.org.ciplima.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.org.ciplima.backend.domain.entity.EventoConfig;
import pe.org.ciplima.backend.dto.EventoEstadoResponse;
import pe.org.ciplima.backend.repository.EventoConfigRepository;

@Service
public class EventoService {

    private static final short EVENTO_ID = 1;

    private final EventoConfigRepository eventoConfigRepository;

    public EventoService(EventoConfigRepository eventoConfigRepository) {
        this.eventoConfigRepository = eventoConfigRepository;
    }

    @Transactional(readOnly = true)
    public EventoEstadoResponse obtenerEstado() {
        EventoConfig evento = obtenerConfiguracionEvento();
        return mapearEstado(evento);
    }

    @Transactional(readOnly = true)
    public boolean tieneCupoDisponible() {
        return obtenerConfiguracionEvento().tieneCupoDisponible();
    }

    @Transactional(readOnly = true)
    public EventoConfig obtenerConfiguracionEvento() {
        return eventoConfigRepository.findById(EVENTO_ID)
                .orElseThrow(() -> new IllegalStateException("No existe la configuración del evento"));
    }

    private EventoEstadoResponse mapearEstado(EventoConfig evento) {
        return new EventoEstadoResponse(
                evento.getCupoMaximo(),
                evento.getCupoOcupado(),
                evento.getCupoDisponible(),
                evento.tieneCupoDisponible(),
                evento.getSedeConsejo()
        );
    }
}
