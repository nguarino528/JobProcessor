package message;

import java.util.Objects;

public record CompletedRunMessage(
        String uploadUrl,
        String runId,
        String status
) {}

