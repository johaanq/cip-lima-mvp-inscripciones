package pe.org.ciplima.backend.storage;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidacionArchivoImagenTest {

    @Test
    void aceptaImagenJpegValida() {
        MockMultipartFile archivo = new MockMultipartFile(
                "imagen", "dni.jpg", "image/jpeg", "contenido".getBytes()
        );

        assertDoesNotThrow(() -> ValidacionArchivoImagen.validar(archivo));
    }

    @Test
    void rechazaArchivoVacio() {
        MockMultipartFile archivo = new MockMultipartFile(
                "imagen", "dni.jpg", "image/jpeg", new byte[0]
        );

        assertThrows(IllegalArgumentException.class, () -> ValidacionArchivoImagen.validar(archivo));
    }

    @Test
    void generaObjectKeyConPrefijoSolicitudes() {
        String objectKey = ValidacionArchivoImagen.generarObjectKey("image/jpeg");

        assertTrue(objectKey.startsWith("solicitudes/"));
        assertTrue(objectKey.endsWith(".jpg"));
    }
}
