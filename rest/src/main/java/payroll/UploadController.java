package payroll;

import message.CompleteUploadRequest;
import message.CompleteUploadResponse;
import message.PresignUploadRequest;
import message.PresignUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import payroll.entity.S3File;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import org.springframework.web.bind.annotation.PostMapping;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/uploads")
public class UploadController {

    @Value("${app.s3.bucket}")
    private String bucket;

    private final S3Presigner presigner;
    private final S3Client s3Client;

    private final FileRepository fileRepository;

    public UploadController(S3Presigner presigner, S3Client s3Client, FileRepository fileRepository) {
        this.presigner = presigner;
        this.s3Client = s3Client;
        this.fileRepository = fileRepository;
    }
    @PostMapping("/presign")
    public PresignUploadResponse presignUpload(@RequestBody PresignUploadRequest request) {
        validate(request);
        String objectKey = "uploads/%s/%s".formatted(
                UUID.randomUUID(),
                sanitizeFilename(request.filename())
        );

        S3File file = new S3File(objectKey, request.sizeBytes(), "PENDING");
        fileRepository.save(file);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(request.contentType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest =
                presigner.presignPutObject(presignRequest);

        return new PresignUploadResponse(
                presignedRequest.url().toString(),
                objectKey,
                file.getFileId().toString()
        );
    }


    @PostMapping("/complete-upload")
    public CompleteUploadResponse completeUpload(@RequestBody CompleteUploadRequest request) {
        Optional<S3File> fileOptional = fileRepository.findByS3Key(request.objectKey());
        if (fileOptional.isEmpty()) {
            throw new RuntimeException("File Not Found");
        }
        S3File s3File = fileOptional.get();
        HeadObjectResponse response =
                s3Client.headObject(
                        HeadObjectRequest.builder()
                                .bucket(bucket)
                                .key(s3File.getS3Key())
                                .build());

        s3File.setStatus("UPLOADED");
        s3File.setS3LastModified(response.lastModified());
        if(s3File.getSizeBytes() != response.contentLength()){
            throw new RuntimeException("File size mismatch");
        }
        fileRepository.save(s3File);

        return new CompleteUploadResponse(
                true
        );
    }

    private void validate(PresignUploadRequest request) {
        if (request.filename() == null || request.filename().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "filename is required");
        }

        if (request.contentType() == null || !request.contentType().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only image uploads are supported");
        }

        if (request.sizeBytes() <= 0 || request.sizeBytes() > 10 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file too large");
        }
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
