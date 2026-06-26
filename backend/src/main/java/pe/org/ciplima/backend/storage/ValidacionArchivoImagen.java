package pe.org.ciplima.backend.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

public final class ValidacionArchivoImagen {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of("image/jpeg", "image/png");
    private static final long TAMANO_MAXIMO_BYTES = 5 * 1024 * 1024;

    private ValidacionArchivoImagen() {
    }

    public static void validar(MultipartFile archivo) {
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

    public static String generarObjectKey(String contentType) {
        String extension = "image/png".equals(contentType) ? ".png" : ".jpg";
        return "solicitudes/" + UUID.randomUUID() + extension;
    }
}
