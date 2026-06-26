package pe.org.ciplima.backend.dto;

public record LoginResponse(
        String token,
        String type,
        long expiresInMs
) {
    public static LoginResponse of(String token, long expiresInMs) {
        return new LoginResponse(token, "Bearer", expiresInMs);
    }
}
