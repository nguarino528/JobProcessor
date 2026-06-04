package message;

public record PresignUploadResponse(
        String uploadUrl,
        String objectKey,
        String fileUUID
) {}
