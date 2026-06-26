package pe.org.ciplima.backend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.org.ciplima.backend.dto.AdminMetricasResponse;
import pe.org.ciplima.backend.service.AdminSolicitudService;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administracion", description = "Operaciones del panel administrador")
@SecurityRequirement(name = "bearerAuth")
public class AdminMetricasController {

    private final AdminSolicitudService adminSolicitudService;

    public AdminMetricasController(AdminSolicitudService adminSolicitudService) {
        this.adminSolicitudService = adminSolicitudService;
    }

    @GetMapping("/metricas")
    public AdminMetricasResponse obtenerMetricas() {
        return adminSolicitudService.obtenerMetricas();
    }
}
