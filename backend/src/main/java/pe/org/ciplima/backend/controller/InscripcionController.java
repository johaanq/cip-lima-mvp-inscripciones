package pe.org.ciplima.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pe.org.ciplima.backend.dto.InscripcionResponse;
import pe.org.ciplima.backend.service.InscripcionService;

@RestController
@RequestMapping("/api/inscripciones")
@Tag(name = "Inscripciones", description = "Registro y consulta de solicitudes publicas")
public class InscripcionController {

    private final InscripcionService inscripcionService;

    public InscripcionController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public InscripcionResponse registrar(
            @RequestParam String dniColegiado,
            @RequestParam String nombreColegiado,
            @RequestParam String dniMenor,
            @RequestParam MultipartFile imagen
    ) {
        return inscripcionService.registrar(dniColegiado, nombreColegiado, dniMenor, imagen);
    }

    @GetMapping("/{id}")
    public InscripcionResponse consultar(@PathVariable Long id) {
        return inscripcionService.consultar(id);
    }
}
