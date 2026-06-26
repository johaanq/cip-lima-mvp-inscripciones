package pe.org.ciplima.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.org.ciplima.backend.dto.EventoEstadoResponse;
import pe.org.ciplima.backend.service.EventoService;

@RestController
@RequestMapping("/api/evento")
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping("/estado")
    public EventoEstadoResponse obtenerEstado() {
        return eventoService.obtenerEstado();
    }
}
