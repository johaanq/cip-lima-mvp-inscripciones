package pe.org.ciplima.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import pe.org.ciplima.backend.client.ColegiadosApiClient;
import pe.org.ciplima.backend.client.dto.ColegiadoExternoDto;
import pe.org.ciplima.backend.domain.entity.SolicitudInscripcion;
import pe.org.ciplima.backend.domain.enums.EstadoSolicitud;
import pe.org.ciplima.backend.domain.enums.OrigenRechazo;
import pe.org.ciplima.backend.dto.InscripcionResponse;
import pe.org.ciplima.backend.exception.AforoCompletoException;
import pe.org.ciplima.backend.repository.SolicitudRepository;
import pe.org.ciplima.backend.service.elegibilidad.ColegiadoValidationService;
import pe.org.ciplima.backend.service.elegibilidad.ResultadoElegibilidad;
import pe.org.ciplima.backend.storage.AlmacenamientoArchivoService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InscripcionServiceTest {

    @Mock
    private EventoService eventoService;

    @Mock
    private ColegiadosApiClient colegiadosApiClient;

    @Mock
    private ColegiadoValidationService colegiadoValidationService;

    @Mock
    private AlmacenamientoArchivoService almacenamientoArchivoService;

    @Mock
    private SolicitudRepository solicitudRepository;

    @InjectMocks
    private InscripcionService inscripcionService;

    private final MultipartFile imagen = new MockMultipartFile(
            "imagen", "dni.jpg", "image/jpeg", "contenido".getBytes()
    );

    @Test
    void registraSolicitudPendienteCuandoColegiadoEsElegible() {
        ColegiadoExternoDto colegiado = new ColegiadoExternoDto(
                "12345678", "Juan Perez", true, false, "Lima"
        );

        when(eventoService.tieneCupoDisponible()).thenReturn(true);
        when(almacenamientoArchivoService.guardarImagenDniMenor(imagen)).thenReturn("solicitudes/uuid.jpg");
        when(colegiadosApiClient.buscarPorDni("12345678")).thenReturn(Optional.of(colegiado));
        when(colegiadoValidationService.evaluar(Optional.of(colegiado)))
                .thenReturn(ResultadoElegibilidad.aprobado());
        when(solicitudRepository.save(any(SolicitudInscripcion.class))).thenAnswer(invocation -> {
            SolicitudInscripcion solicitud = invocation.getArgument(0);
            return solicitud;
        });

        InscripcionResponse response = inscripcionService.registrar(
                "12345678", "Juan Perez", "11223344", imagen
        );

        assertEquals(EstadoSolicitud.PENDIENTE, response.estado());
        verify(solicitudRepository).save(any(SolicitudInscripcion.class));
    }

    @Test
    void registraSolicitudRechazadaCuandoColegiadoNoEsElegible() {
        ColegiadoExternoDto colegiado = new ColegiadoExternoDto(
                "87654321", "Maria Lopez", true, true, "Lima"
        );

        when(eventoService.tieneCupoDisponible()).thenReturn(true);
        when(almacenamientoArchivoService.guardarImagenDniMenor(imagen)).thenReturn("solicitudes/uuid.jpg");
        when(colegiadosApiClient.buscarPorDni("87654321")).thenReturn(Optional.of(colegiado));
        when(colegiadoValidationService.evaluar(Optional.of(colegiado)))
                .thenReturn(ResultadoElegibilidad.rechazado("El personal administrativo no puede inscribirse en este evento social"));

        when(solicitudRepository.save(any(SolicitudInscripcion.class))).thenAnswer(invocation -> {
            SolicitudInscripcion solicitud = invocation.getArgument(0);
            assertEquals(EstadoSolicitud.RECHAZADO, solicitud.getEstado());
            assertEquals(OrigenRechazo.AUTOMATICO, solicitud.getOrigenRechazo());
            return solicitud;
        });

        InscripcionResponse response = inscripcionService.registrar(
                "87654321", "Maria Lopez", "11223344", imagen
        );

        assertEquals(EstadoSolicitud.RECHAZADO, response.estado());
    }

    @Test
    void lanzaExcepcionCuandoAforoEstaCompleto() {
        when(eventoService.tieneCupoDisponible()).thenReturn(false);

        assertThrows(AforoCompletoException.class, () ->
                inscripcionService.registrar("12345678", "Juan Perez", "11223344", imagen)
        );
    }
}
