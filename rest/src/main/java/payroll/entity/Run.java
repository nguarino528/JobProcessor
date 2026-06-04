package payroll.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
public class Run {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "run_id")
    private UUID runId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonIgnore
    private Job job;
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    @Column(name = "s3_output_key")
    private String s3OutputKey;

    protected Run() {}

    public Run(String status) {
        this.status = status;
    }

    public UUID getRunId() {
        return runId;
    }

    public void setRunId(UUID runId) {
        this.runId = runId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getS3OutputKey() {
        return s3OutputKey;
    }

    public void setS3OutputKey(String s3OutputKey) {
        this.s3OutputKey = s3OutputKey;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Run run = (Run) o;
        return Objects.equals(runId, run.runId) && Objects.equals(job, run.job) && Objects.equals(status, run.status) && Objects.equals(createdAt, run.createdAt) && Objects.equals(s3OutputKey, run.s3OutputKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(runId, job, status, createdAt, s3OutputKey);
    }

    @Override
    public String toString() {
        return "Run{runId=" + runId + ", status='" + status + "'}";
    }
}