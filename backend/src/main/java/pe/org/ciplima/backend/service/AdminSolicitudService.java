package pe.org.ciplima.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.org.ciplima.backend.domain.entity.EventoConfig;
import pe.org.ciplima.backend.domain.entity.SolicitudInscripcion;
import pe.org.ciplima.backend.domain.enums.EstadoSolicitud;
import pe.org.ciplima.backend.dto.AdminMetricasResponse;
import pe.org.ciplima.backend.dto.InscripcionResponse;
import pe.org.ciplima.backend.dto.SolicitudHistorialResponse;
import pe.org.ciplima.backend.dto.SolicitudPendienteResponse;
import pe.org.ciplima.backend.repository.EventoConfigRepository;
import pe.org.ciplima.backend.repository.SolicitudRepository;
import pe.org.ciplima.backend.storage.AlmacenamientoArchivoService;
import pe.org.ciplima.backend.storage.ArchivoImagenResponse;

import java.util.List;

@Service
public class AdminSolicitudService {

    private static final short EVENTO_ID = 1;

    private final SolicitudRepository solicitudRepository;
    private final EventoConfigRepository eventoConfigRepository;
    private final EventoService eventoService;
    private final AlmacenamientoArchivoService almacenamientoArchivoService;
    private final NotificacionService notificacionService;

    public AdminSolicitudService(
            SolicitudRepository solicitudRepository,
            EventoConfigRepository eventoConfigRepository,
            EventoService eventoService,
            AlmacenamientoArchivoService almacenamientoArchivoService,
            NotificacionService notificacionService
    ) {
        this.solicitudRepository = solicitudRepository;
        this.eventoConfigRepository = eventoConfigRepository;
        this.eventoService = eventoService;
        this.almacenamientoArchivoService = almacenamientoArchivoService;
        this.notificacionService = notificacionService;
    }

    @Transactional(readOnly = true)
    public AdminMetricasResponse obtenerMetricas() {
        EventoConfig evento = eventoService.obtenerConfiguracionEvento();

        return new AdminMetricasResponse(
                solicitudRepository.countTotal(),
                solicitudRepository.countByEstado(EstadoSolicitud.APROBADO),
                solicitudRepository.countByEstado(EstadoSolicitud.RECHAZADO),
                solicitudRepository.countByEstado(EstadoSolicitud.PENDIENTE),
                evento.getCupoDisponible()
        );
    }

    @Transactional(readOnly = true)
    public List<SolicitudPendienteResponse> listarPendientes() {
        return solicitudRepository.findByEstadoOrderByCreatedAtAsc(EstadoSolicitud.PENDIENTE).stream()
                .map(this::mapearPendiente)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitudHistorialResponse> listarAprobadas() {
        return solicitudRepository.findByEstadoOrderByUpdatedAtDesc(EstadoSolicitud.APROBADO).stream()
                .map(this::mapearHistorial)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitudHistorialResponse> listarRechazadas() {
        return solicitudRepository.findByEstadoOrderByUpdatedAtDesc(EstadoSolicitud.RECHAZADO).stream()
                .map(this::mapearHistorial)
                .toList();
    }

    @Transactional
    public InscripcionResponse aprobar(Long solicitudId) {
        SolicitudInscripcion solicitud = obtenerSolicitud(solicitudId);

        int cupoActualizado = eventoConfigRepository.incrementarCupoSiDisponible(EVENTO_ID);
        if (cupoActualizado == 0) {
            throw new IllegalStateException("No hay cupo disponible para aprobar esta solicitud");
        }

        solicitud.aprobar();
        SolicitudInscripcion guardada = solicitudRepository.save(solicitud);

        EventoConfig evento = eventoService.obtenerConfiguracionEvento();
        notificacionService.enviarInvitacion(guardada, evento);

        return mapearRespuesta(guardada, "La solicitud fue aprobada correctamente");
    }

    @Transactional
    public InscripcionResponse rechazar(Long solicitudId, String observacion) {
        SolicitudInscripcion solicitud = obtenerSolicitud(solicitudId);
        solicitud.rechazarPorAdmin(observacion);
        SolicitudInscripcion guardada = solicitudRepository.save(solicitud);

        notificacionService.enviarAlertaRechazo(guardada);

        return mapearRespuesta(guardada, "La solicitud fue rechazada por el administrador");
    }

    @Transactional(readOnly = true)
    public ArchivoImagenResponse obtenerImagenSolicitud(Long solicitudId) {
        SolicitudInscripcion solicitud = obtenerSolicitud(solicitudId);

        if (solicitud.getImagenObjectKey() == null || solicitud.getImagenObjectKey().isBlank()) {
            throw new IllegalArgumentException("La solicitud no tiene imagen asociada");
        }

        return almacenamientoArchivoService.obtenerImagen(solicitud.getImagenObjectKey());
    }

    private SolicitudInscripcion obtenerSolicitud(Long solicitudId) {
        return solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la solicitud indicada"));
    }

    private SolicitudPendienteResponse mapearPendiente(SolicitudInscripcion solicitud) {
        return new SolicitudPendienteResponse(
                solicitud.getId(),
                solicitud.getDniColegiado(),
                solicitud.getNombreColegiado(),
                solicitud.getDniMenor(),
                solicitud.getCreatedAt()
        );
    }

    private SolicitudHistorialResponse mapearHistorial(SolicitudInscripcion solicitud) {
        return new SolicitudHistorialResponse(
                solicitud.getId(),
                solicitud.getDniColegiado(),
                solicitud.getNombreColegiado(),
                solicitud.getDniMenor(),
                solicitud.getCreatedAt(),
                solicitud.getUpdatedAt(),
                solicitud.getMotivoRechazo(),
                solicitud.getOrigenRechazo()
        );
    }

    private InscripcionResponse mapearRespuesta(SolicitudInscripcion solicitud, String mensaje) {
        return new InscripcionResponse(
                solicitud.getId(),
                solicitud.getEstado(),
                solicitud.getMotivoRechazo(),
                mensaje
        );
    }
}
