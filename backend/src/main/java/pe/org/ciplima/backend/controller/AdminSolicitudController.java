package pe.org.ciplima.backend.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.org.ciplima.backend.service.AdminSolicitudService;
import pe.org.ciplima.backend.storage.ArchivoImagenResponse;

@RestController
@RequestMapping("/api/admin/solicitudes")
public class AdminSolicitudController {

    private final AdminSolicitudService adminSolicitudService;

    public AdminSolicitudController(AdminSolicitudService adminSolicitudService) {
        this.adminSolicitudService = adminSolicitudService;
    }

    @GetMapping("/{id}/imagen")
    public ResponseEntity<InputStreamResource> obtenerImagen(@PathVariable Long id) {
        ArchivoImagenResponse imagen = adminSolicitudService.obtenerImagenSolicitud(id);

        MediaType mediaType = imagen.contentType() != null
                ? MediaType.parseMediaType(imagen.contentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(imagen.contentLength())
                .body(new InputStreamResource(imagen.contenido()));
    }
}
