package pe.org.ciplima.backend.storage;

import org.springframework.web.multipart.MultipartFile;

public interface AlmacenamientoArchivoService {

    String guardarImagenDniMenor(MultipartFile archivo);

    ArchivoImagenResponse obtenerImagen(String objectKey);
}
