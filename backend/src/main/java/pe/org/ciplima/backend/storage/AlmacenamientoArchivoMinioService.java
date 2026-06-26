package pe.org.ciplima.backend.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.org.ciplima.backend.config.AppProperties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

@Service
public class AlmacenamientoArchivoMinioService implements AlmacenamientoArchivoService {

    private final S3Client s3Client;
    private final String bucket;

    public AlmacenamientoArchivoMinioService(S3Client s3Client, AppProperties appProperties) {
        this.s3Client = s3Client;
        this.bucket = appProperties.minio().bucket();
    }

    @Override
    public String guardarImagenDniMenor(MultipartFile archivo) {
        ValidacionArchivoImagen.validar(archivo);

        String objectKey = ValidacionArchivoImagen.generarObjectKey(archivo.getContentType());

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(archivo.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(archivo.getBytes()));
            return objectKey;
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible leer la imagen enviada", ex);
        } catch (S3Exception ex) {
            throw new IllegalStateException("No fue posible almacenar la imagen en MinIO", ex);
        }
    }

    @Override
    public ArchivoImagenResponse obtenerImagen(String objectKey) {
        try {
            var response = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build());

            return new ArchivoImagenResponse(
                    response,
                    response.response().contentType(),
                    response.response().contentLength()
            );
        } catch (NoSuchKeyException ex) {
            throw new IllegalArgumentException("No se encontró la imagen solicitada");
        } catch (S3Exception ex) {
            throw new IllegalStateException("No fue posible obtener la imagen desde MinIO", ex);
        }
    }
}
