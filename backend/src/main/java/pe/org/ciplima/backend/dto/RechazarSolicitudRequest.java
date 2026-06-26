package pe.org.ciplima.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record RechazarSolicitudRequest(
        @NotBlank(message = "La observación de rechazo es obligatoria")
        String observacion
) {
}
