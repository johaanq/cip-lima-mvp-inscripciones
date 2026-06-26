package pe.org.ciplima.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.org.ciplima.backend.domain.entity.SolicitudInscripcion;
import pe.org.ciplima.backend.repository.SolicitudRepository;
import pe.org.ciplima.backend.storage.AlmacenamientoArchivoService;
import pe.org.ciplima.backend.storage.ArchivoImagenResponse;

@Service
public class AdminSolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final AlmacenamientoArchivoService almacenamientoArchivoService;

    public AdminSolicitudService(
            SolicitudRepository solicitudRepository,
            AlmacenamientoArchivoService almacenamientoArchivoService
    ) {
        this.solicitudRepository = solicitudRepository;
        this.almacenamientoArchivoService = almacenamientoArchivoService;
    }

    @Transactional(readOnly = true)
    public ArchivoImagenResponse obtenerImagenSolicitud(Long solicitudId) {
        SolicitudInscripcion solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la solicitud indicada"));

        if (solicitud.getImagenObjectKey() == null || solicitud.getImagenObjectKey().isBlank()) {
            throw new IllegalArgumentException("La solicitud no tiene imagen asociada");
        }

        return almacenamientoArchivoService.obtenerImagen(solicitud.getImagenObjectKey());
    }
}
