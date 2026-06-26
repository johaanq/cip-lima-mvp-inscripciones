package pe.org.ciplima.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        EventoProperties evento,
        ColegiadosApiProperties colegiadosApi,
        JwtProperties jwt,
        MinioProperties minio,
        CorsProperties cors
) {

    public record EventoProperties(int cupoMaximo, String sedeConsejo) {
    }

    public record ColegiadosApiProperties(String baseUrl) {
    }

    public record JwtProperties(String secret, long expirationMs) {
    }

    public record MinioProperties(String endpoint, String accessKey, String secretKey, String bucket) {
    }

    public record CorsProperties(String allowedOrigins) {
    }
}
