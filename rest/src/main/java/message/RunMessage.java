package message;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.Objects;
import java.util.UUID;

public class RunMessage {
    private UUID runId;
    private Long jobId;
    private String status;

    public RunMessage(UUID runId, Long jobId, String status) {
        this.runId = runId;
        this.jobId = jobId;
        this.status = status;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RunMessage that = (RunMessage) o;
        return Objects.equals(runId, that.runId) && Objects.equals(jobId, that.jobId) && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(runId, jobId, status);
    }
}
