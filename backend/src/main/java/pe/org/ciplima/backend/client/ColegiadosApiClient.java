package pe.org.ciplima.backend.client;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.org.ciplima.backend.client.dto.ColegiadoExternoDto;
import pe.org.ciplima.backend.config.AppProperties;

import java.util.Arrays;
import java.util.Optional;

@Component
public class ColegiadosApiClient {

    private final RestClient restClient;

    public ColegiadosApiClient(RestClient.Builder restClientBuilder, AppProperties appProperties) {
        this.restClient = restClientBuilder
                .baseUrl(appProperties.colegiadosApi().baseUrl())
                .build();
    }

    public Optional<ColegiadoExternoDto> buscarPorDni(String dni) {
        try {
            ColegiadoExternoDto[] colegiados = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/colegiados")
                            .queryParam("dni", dni)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new ColegiadosApiException(
                                "Error al consultar API de colegiados: HTTP " + response.getStatusCode().value()
                        );
                    })
                    .body(ColegiadoExternoDto[].class);

            if (colegiados == null || colegiados.length == 0) {
                return Optional.empty();
            }

            return Arrays.stream(colegiados)
                    .filter(colegiado -> dni.equals(colegiado.dni()))
                    .findFirst();
        } catch (RestClientException ex) {
            throw new ColegiadosApiException("No fue posible comunicarse con la API de colegiados", ex);
        }
    }
}
