package pe.org.ciplima.backend.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

/**
 * Implementación temporal en disco local. Será reemplazada por MinIO en R4.
 */
@Service
public class AlmacenamientoArchivoTemporalService implements AlmacenamientoArchivoService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of("image/jpeg", "image/png");
    private static final long TAMANO_MAXIMO_BYTES = 5 * 1024 * 1024;

    private Path directorioBase;

    @PostConstruct
    void init() throws IOException {
        directorioBase = Path.of(System.getProperty("java.io.tmpdir"), "cip-inscripciones", "uploads");
        Files.createDirectories(directorioBase);
    }

    @Override
    public String guardarImagenDniMenor(MultipartFile archivo) {
        validarArchivo(archivo);

        String extension = obtenerExtension(archivo.getContentType());
        String objectKey = "solicitudes/" + UUID.randomUUID() + extension;
        Path destino = directorioBase.resolve(objectKey.replace("/", "_"));

        try {
            Files.createDirectories(destino.getParent());
            archivo.transferTo(destino);
            return objectKey;
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible almacenar la imagen del DNI del menor", ex);
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("La imagen del DNI del menor es obligatoria");
        }
        if (archivo.getSize() > TAMANO_MAXIMO_BYTES) {
            throw new IllegalArgumentException("La imagen no debe superar los 5 MB");
        }
        String contentType = archivo.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType)) {
            throw new IllegalArgumentException("Solo se permiten imágenes JPEG o PNG");
        }
    }

    private String obtenerExtension(String contentType) {
        return "image/png".equals(contentType) ? ".png" : ".jpg";
    }
}
