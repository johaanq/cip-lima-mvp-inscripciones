package pe.org.ciplima.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.org.ciplima.backend.domain.entity.EventoConfig;
import pe.org.ciplima.backend.dto.EventoEstadoResponse;
import pe.org.ciplima.backend.repository.EventoConfigRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock
    private EventoConfigRepository eventoConfigRepository;

    @InjectMocks
    private EventoService eventoService;

    @Test
    void obtenerEstadoCuandoHayCupoDisponible() {
        EventoConfig evento = new EventoConfig(10, 3, "Lima");
        when(eventoConfigRepository.findById((short) 1)).thenReturn(Optional.of(evento));

        EventoEstadoResponse estado = eventoService.obtenerEstado();

        assertEquals(10, estado.cupoMaximo());
        assertEquals(3, estado.cupoOcupado());
        assertEquals(7, estado.cupoDisponible());
        assertTrue(estado.inscripcionesAbiertas());
        assertEquals("Lima", estado.sedeConsejo());
    }

    @Test
    void obtenerEstadoCuandoAforoEstaLleno() {
        EventoConfig evento = new EventoConfig(10, 10, "Lima");
        when(eventoConfigRepository.findById((short) 1)).thenReturn(Optional.of(evento));

        EventoEstadoResponse estado = eventoService.obtenerEstado();

        assertEquals(0, estado.cupoDisponible());
        assertFalse(estado.inscripcionesAbiertas());
    }

    @Test
    void tieneCupoDisponibleRetornaFalseCuandoAforoCompleto() {
        EventoConfig evento = new EventoConfig(10, 10, "Lima");
        when(eventoConfigRepository.findById((short) 1)).thenReturn(Optional.of(evento));

        assertFalse(eventoService.tieneCupoDisponible());
    }
}
