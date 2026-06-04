package message;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RunMessage {
    private UUID runId;
    private Long jobId;
    private String status;
    List<String> s3Keys;

    public RunMessage(UUID runId, Long jobId, String status, List<String> s3Keys) {
        this.runId = runId;
        this.jobId = jobId;
        this.status = status;
        this.s3Keys = s3Keys;
    }

    public UUID getRunId() {
        return runId;
    }

    public void setRunId(UUID runId) {
        this.runId = runId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getS3Keys() {
        return s3Keys;
    }

    public void setS3Keys(List<String> s3Keys) {
        this.s3Keys = s3Keys;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RunMessage that = (RunMessage) o;
        return Objects.equals(getRunId(), that.getRunId()) && Objects.equals(getJobId(), that.getJobId()) && Objects.equals(getStatus(), that.getStatus()) && Objects.equals(getS3Keys(), that.getS3Keys());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRunId(), getJobId(), getStatus(), getS3Keys());
    }
}
