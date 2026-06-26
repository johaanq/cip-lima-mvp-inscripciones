package pe.org.ciplima.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.org.ciplima.backend.domain.entity.SolicitudInscripcion;
import pe.org.ciplima.backend.domain.enums.EstadoSolicitud;
import pe.org.ciplima.backend.domain.enums.OrigenRechazo;
import pe.org.ciplima.backend.repository.SolicitudRepository;
import pe.org.ciplima.backend.storage.AlmacenamientoArchivoService;
import pe.org.ciplima.backend.storage.ArchivoImagenResponse;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSolicitudServiceTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private AlmacenamientoArchivoService almacenamientoArchivoService;

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
}
