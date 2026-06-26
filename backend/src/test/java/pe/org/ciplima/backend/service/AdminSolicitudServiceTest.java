package pe.org.ciplima.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.org.ciplima.backend.domain.entity.EventoConfig;
import pe.org.ciplima.backend.domain.entity.SolicitudInscripcion;
import pe.org.ciplima.backend.domain.enums.EstadoSolicitud;
import pe.org.ciplima.backend.dto.AdminMetricasResponse;
import pe.org.ciplima.backend.dto.InscripcionResponse;
import pe.org.ciplima.backend.dto.SolicitudPendienteResponse;
import pe.org.ciplima.backend.repository.EventoConfigRepository;
import pe.org.ciplima.backend.repository.SolicitudRepository;
import pe.org.ciplima.backend.storage.AlmacenamientoArchivoService;
import pe.org.ciplima.backend.storage.ArchivoImagenResponse;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSolicitudServiceTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private EventoConfigRepository eventoConfigRepository;

    @Mock
    private EventoService eventoService;

    @Mock
    private AlmacenamientoArchivoService almacenamientoArchivoService;

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private AdminSolicitudService adminSolicitudService;

    @Test
    void obtieneImagenDeSolicitudExistente() {
        SolicitudInscripcion solicitud = SolicitudInscripcion.crearPendiente(
                "12345678", "Juan Perez", "11223344", "solicitudes/uuid.jpg"
        );

        ArchivoImagenResponse imagen = new ArchivoImagenResponse(
                new ByteArrayInputStream("bytes".getBytes()),
                "image/jpeg",
                5
        );

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(almacenamientoArchivoService.obtenerImagen("solicitudes/uuid.jpg")).thenReturn(imagen);

        ArchivoImagenResponse resultado = adminSolicitudService.obtenerImagenSolicitud(1L);

        assertEquals("image/jpeg", resultado.contentType());
    }

    @Test
    void obtieneMetricasAgregadas() {
        EventoConfig evento = new EventoConfig(10, 3, "Lima");

        when(solicitudRepository.countTotal()).thenReturn(8L);
        when(solicitudRepository.countByEstado(EstadoSolicitud.APROBADO)).thenReturn(3L);
        when(solicitudRepository.countByEstado(EstadoSolicitud.RECHAZADO)).thenReturn(2L);
        when(solicitudRepository.countByEstado(EstadoSolicitud.PENDIENTE)).thenReturn(3L);
        when(eventoService.obtenerConfiguracionEvento()).thenReturn(evento);

        AdminMetricasResponse metricas = adminSolicitudService.obtenerMetricas();

        assertEquals(8L, metricas.total());
        assertEquals(3L, metricas.aprobados());
        assertEquals(2L, metricas.rechazados());
        assertEquals(3L, metricas.pendientes());
        assertEquals(7, metricas.cupoDisponible());
    }

    @Test
    void listaSolicitudesPendientes() {
        SolicitudInscripcion solicitud = SolicitudInscripcion.crearPendiente(
                "12345678", "Juan Perez", "11223344", "solicitudes/uuid.jpg"
        );

        when(solicitudRepository.findByEstadoOrderByCreatedAtAsc(EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of(solicitud));

        List<SolicitudPendienteResponse> pendientes = adminSolicitudService.listarPendientes();

        assertEquals(1, pendientes.size());
        assertEquals("12345678", pendientes.get(0).dniColegiado());
    }

    @Test
    void apruebaSolicitudPendienteConCupoDisponible() {
        SolicitudInscripcion solicitud = SolicitudInscripcion.crearPendiente(
                "12345678", "Juan Perez", "11223344", "solicitudes/uuid.jpg"
        );
        EventoConfig evento = new EventoConfig(10, 4, "Lima");

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(eventoConfigRepository.incrementarCupoSiDisponible((short) 1)).thenReturn(1);
        when(solicitudRepository.save(solicitud)).thenReturn(solicitud);
        when(eventoService.obtenerConfiguracionEvento()).thenReturn(evento);

        InscripcionResponse respuesta = adminSolicitudService.aprobar(1L);

        assertEquals(EstadoSolicitud.APROBADO, respuesta.estado());
        verify(notificacionService).enviarInvitacion(solicitud, evento);
    }

    @Test
    void rechazaAprobacionCuandoNoHayCupo() {
        SolicitudInscripcion solicitud = SolicitudInscripcion.crearPendiente(
                "12345678", "Juan Perez", "11223344", "solicitudes/uuid.jpg"
        );

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(eventoConfigRepository.incrementarCupoSiDisponible((short) 1)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> adminSolicitudService.aprobar(1L));
        verify(solicitudRepository, never()).save(any());
        verify(notificacionService, never()).enviarInvitacion(any(), any());
    }

    @Test
    void rechazaSolicitudPendienteConObservacion() {
        SolicitudInscripcion solicitud = SolicitudInscripcion.crearPendiente(
                "12345678", "Juan Perez", "11223344", "solicitudes/uuid.jpg"
        );

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(solicitud)).thenReturn(solicitud);

        InscripcionResponse respuesta = adminSolicitudService.rechazar(1L, "Imagen ilegible");

        assertEquals(EstadoSolicitud.RECHAZADO, respuesta.estado());
        assertEquals("Imagen ilegible", respuesta.motivoRechazo());
        verify(notificacionService).enviarAlertaRechazo(solicitud);
    }
}
