package message;

public record PresignUploadRequest(
        String filename,
        String contentType,
        long sizeBytes
) {}
