package pe.org.ciplima.backend.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import pe.org.ciplima.backend.config.AppProperties;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
public class MinioBucketInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MinioBucketInitializer.class);

    private final S3Client s3Client;
    private final AppProperties appProperties;

    public MinioBucketInitializer(S3Client s3Client, AppProperties appProperties) {
        this.s3Client = s3Client;
        this.appProperties = appProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        String bucket = appProperties.minio().bucket();

        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            log.info("Bucket MinIO '{}' disponible", bucket);
        } catch (NoSuchBucketException ex) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            log.info("Bucket MinIO '{}' creado", bucket);
        } catch (S3Exception ex) {
            log.warn("No fue posible verificar el bucket MinIO '{}': {}", bucket, ex.getMessage());
        }
    }
}
