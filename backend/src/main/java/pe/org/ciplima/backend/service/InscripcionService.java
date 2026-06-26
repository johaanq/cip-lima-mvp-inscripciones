package pe.org.ciplima.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Service
public class InscripcionService {

    private final EventoService eventoService;
    private final ColegiadosApiClient colegiadosApiClient;
    private final ColegiadoValidationService colegiadoValidationService;
    private final AlmacenamientoArchivoService almacenamientoArchivoService;
    private final SolicitudRepository solicitudRepository;

    public InscripcionService(
            EventoService eventoService,
            ColegiadosApiClient colegiadosApiClient,
            ColegiadoValidationService colegiadoValidationService,
            AlmacenamientoArchivoService almacenamientoArchivoService,
            SolicitudRepository solicitudRepository
    ) {
        this.eventoService = eventoService;
        this.colegiadosApiClient = colegiadosApiClient;
        this.colegiadoValidationService = colegiadoValidationService;
        this.almacenamientoArchivoService = almacenamientoArchivoService;
        this.solicitudRepository = solicitudRepository;
    }

    @Transactional
    public InscripcionResponse registrar(
            String dniColegiado,
            String nombreColegiado,
            String dniMenor,
            MultipartFile imagen
    ) {
        validarDatosEntrada(dniColegiado, nombreColegiado, dniMenor);

        if (!eventoService.tieneCupoDisponible()) {
            throw new AforoCompletoException();
        }

        String imagenObjectKey = almacenamientoArchivoService.guardarImagenDniMenor(imagen);

        ColegiadoExternoDto colegiado = colegiadosApiClient.buscarPorDni(dniColegiado).orElse(null);
        ResultadoElegibilidad resultado = colegiadoValidationService.evaluar(
                Optional.ofNullable(colegiado)
        );

        SolicitudInscripcion solicitud;
        if (resultado.elegible()) {
            solicitud = SolicitudInscripcion.crearPendiente(
                    dniColegiado, nombreColegiado, dniMenor, imagenObjectKey
            );
        } else {
            solicitud = SolicitudInscripcion.crearRechazada(
                    dniColegiado,
                    nombreColegiado,
                    dniMenor,
                    imagenObjectKey,
                    resultado.motivoRechazo(),
                    OrigenRechazo.AUTOMATICO
            );
        }

        SolicitudInscripcion guardada = solicitudRepository.save(solicitud);
        return mapearRespuesta(guardada);
    }

    @Transactional(readOnly = true)
    public InscripcionResponse consultar(Long id) {
        SolicitudInscripcion solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la solicitud indicada"));
        return mapearRespuesta(solicitud);
    }

    private void validarDatosEntrada(String dniColegiado, String nombreColegiado, String dniMenor) {
        validarDni(dniColegiado, "DNI del colegiado");
        validarDni(dniMenor, "DNI del menor");
        if (nombreColegiado == null || nombreColegiado.isBlank()) {
            throw new IllegalArgumentException("El nombre del colegiado es obligatorio");
        }
    }

    private void validarDni(String dni, String campo) {
        if (dni == null || !dni.matches("\\d{8}")) {
            throw new IllegalArgumentException(campo + " debe contener exactamente 8 dígitos");
        }
    }

    private InscripcionResponse mapearRespuesta(SolicitudInscripcion solicitud) {
        String mensaje = switch (solicitud.getEstado()) {
            case PENDIENTE -> "Solicitud registrada y pendiente de revisión administrativa";
            case RECHAZADO -> "La solicitud fue rechazada automáticamente";
            case APROBADO -> "La solicitud ya se encuentra aprobada";
        };

        return new InscripcionResponse(
                solicitud.getId(),
                solicitud.getEstado(),
                solicitud.getMotivoRechazo(),
                mensaje
        );
    }
}
