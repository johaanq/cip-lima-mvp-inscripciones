package pe.org.ciplima.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import pe.org.ciplima.backend.domain.enums.EstadoSolicitud;
import pe.org.ciplima.backend.domain.enums.OrigenRechazo;

import java.time.OffsetDateTime;

@Entity
@Table(name = "solicitud_inscripcion")
public class SolicitudInscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dni_colegiado", nullable = false, length = 8)
    private String dniColegiado;

    @Column(name = "nombre_colegiado", nullable = false, length = 200)
    private String nombreColegiado;

    @Column(name = "dni_menor", nullable = false, length = 8)
    private String dniMenor;

    @Column(name = "imagen_object_key", length = 500)
    private String imagenObjectKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_rechazo", length = 20)
    private OrigenRechazo origenRechazo;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected SolicitudInscripcion() {
    }

    public static SolicitudInscripcion crearPendiente(
            String dniColegiado,
            String nombreColegiado,
            String dniMenor,
            String imagenObjectKey
    ) {
        SolicitudInscripcion solicitud = new SolicitudInscripcion();
        solicitud.dniColegiado = dniColegiado;
        solicitud.nombreColegiado = nombreColegiado;
        solicitud.dniMenor = dniMenor;
        solicitud.imagenObjectKey = imagenObjectKey;
        solicitud.estado = EstadoSolicitud.PENDIENTE;
        return solicitud;
    }

    public static SolicitudInscripcion crearRechazada(
            String dniColegiado,
            String nombreColegiado,
            String dniMenor,
            String imagenObjectKey,
            String motivoRechazo,
            OrigenRechazo origenRechazo
    ) {
        SolicitudInscripcion solicitud = new SolicitudInscripcion();
        solicitud.dniColegiado = dniColegiado;
        solicitud.nombreColegiado = nombreColegiado;
        solicitud.dniMenor = dniMenor;
        solicitud.imagenObjectKey = imagenObjectKey;
        solicitud.estado = EstadoSolicitud.RECHAZADO;
        solicitud.motivoRechazo = motivoRechazo;
        solicitud.origenRechazo = origenRechazo;
        return solicitud;
    }

    public void aprobar() {
        if (estado != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden aprobar solicitudes pendientes");
        }
        estado = EstadoSolicitud.APROBADO;
    }

    public void rechazarPorAdmin(String observacion) {
        if (estado != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden rechazar solicitudes pendientes");
        }
        if (observacion == null || observacion.isBlank()) {
            throw new IllegalArgumentException("La observación de rechazo es obligatoria");
        }
        estado = EstadoSolicitud.RECHAZADO;
        motivoRechazo = observacion.trim();
        origenRechazo = OrigenRechazo.ADMIN;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getDniColegiado() {
        return dniColegiado;
    }

    public String getNombreColegiado() {
        return nombreColegiado;
    }

    public String getDniMenor() {
        return dniMenor;
    }

    public String getImagenObjectKey() {
        return imagenObjectKey;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public OrigenRechazo getOrigenRechazo() {
        return origenRechazo;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
