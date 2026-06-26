package pe.org.ciplima.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pe.org.ciplima.backend.domain.entity.EventoConfig;
import pe.org.ciplima.backend.domain.entity.SolicitudInscripcion;

@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    public void enviarInvitacion(SolicitudInscripcion solicitud, EventoConfig evento) {
        log.info(
                "[NOTIFICACION] Invitación enviada a {} (DNI {}) por inscripción aprobada del menor DNI {}. Sede: {}",
                solicitud.getNombreColegiado(),
                solicitud.getDniColegiado(),
                solicitud.getDniMenor(),
                evento.getSedeConsejo()
        );
    }

    public void enviarAlertaRechazo(SolicitudInscripcion solicitud) {
        log.info(
                "[NOTIFICACION] Alerta de rechazo enviada a {} (DNI {}). Motivo: {}",
                solicitud.getNombreColegiado(),
                solicitud.getDniColegiado(),
                solicitud.getMotivoRechazo()
        );
    }
}
