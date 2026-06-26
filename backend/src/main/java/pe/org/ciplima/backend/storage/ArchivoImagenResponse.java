package pe.org.ciplima.backend.storage;

import java.io.InputStream;

public record ArchivoImagenResponse(
        InputStream contenido,
        String contentType,
        long contentLength
) {
}
