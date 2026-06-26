package pe.org.ciplima.backend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.org.ciplima.backend.dto.InscripcionResponse;
import pe.org.ciplima.backend.dto.RechazarSolicitudRequest;
import pe.org.ciplima.backend.dto.SolicitudPendienteResponse;
import pe.org.ciplima.backend.service.AdminSolicitudService;
import pe.org.ciplima.backend.storage.ArchivoImagenResponse;

import java.util.List;

@RestController
@RequestMapping("/api/admin/solicitudes")
@Tag(name = "Administracion", description = "Operaciones del panel administrador")
@SecurityRequirement(name = "bearerAuth")
public class AdminSolicitudController {

    private final AdminSolicitudService adminSolicitudService;

    public AdminSolicitudController(AdminSolicitudService adminSolicitudService) {
        this.adminSolicitudService = adminSolicitudService;
    }

    @GetMapping("/pendientes")
    public List<SolicitudPendienteResponse> listarPendientes() {
        return adminSolicitudService.listarPendientes();
    }

    @PostMapping("/{id}/aprobar")
    public InscripcionResponse aprobar(@PathVariable Long id) {
        return adminSolicitudService.aprobar(id);
    }

    @PostMapping("/{id}/rechazar")
    @ResponseStatus(HttpStatus.OK)
    public InscripcionResponse rechazar(
            @PathVariable Long id,
            @Valid @RequestBody RechazarSolicitudRequest request
    ) {
        return adminSolicitudService.rechazar(id, request.observacion());
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
