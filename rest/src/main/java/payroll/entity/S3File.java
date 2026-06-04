package payroll.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
public class S3File {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "file_id")
    private UUID fileId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "job_id", nullable = true)
    @JsonIgnore
    private Job job;

    @Column(name = "s3_key", unique = true, nullable = false)
    private String s3Key;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "s3_last_modified")
    private Instant s3LastModified;

    @Column
    private long sizeBytes ;

    private String status;

    public S3File(){

    }

    public S3File(String s3Key, long sizeBytes, String status) {
        this.s3Key = s3Key;
        this.sizeBytes = sizeBytes;
        this.status = status;
    }

    public UUID getFileId() {
        return fileId;
    }

    public Job getJob() {
        return job;
    }

    public String getS3Key() {
        return s3Key;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getS3LastModified() {
        return s3LastModified;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setS3LastModified(Instant s3LastModified) {
        this.s3LastModified = s3LastModified;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
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
        S3File s3File = (S3File) o;
        return sizeBytes == s3File.sizeBytes && Objects.equals(fileId, s3File.fileId) && Objects.equals(job, s3File.job) && Objects.equals(s3Key, s3File.s3Key) && Objects.equals(createdAt, s3File.createdAt) && Objects.equals(s3LastModified, s3File.s3LastModified) && Objects.equals(status, s3File.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, job, s3Key, createdAt, s3LastModified, sizeBytes, status);
    }
}
