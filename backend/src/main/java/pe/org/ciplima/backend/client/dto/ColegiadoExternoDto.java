package pe.org.ciplima.backend.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representa un colegiado expuesto por la API externa mock (json-server).
 */
public record ColegiadoExternoDto(
        String dni,
        String nombre,
        boolean habilitado,
        @JsonProperty("es_administrativo") boolean esAdministrativo,
        @JsonProperty("consejo_departamental") String consejoDepartamental
) {
}
