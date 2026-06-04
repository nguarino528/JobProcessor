package payroll.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

@Entity
public class Job {

    @Id
    @GeneratedValue
    @Column(name = "job_id", updatable = false)
    private Long jobId;

    @OneToMany(mappedBy = "job", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Run> run;

    @OneToMany(mappedBy = "job", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<S3File> s3Files;

    private String status;

    public Job() {}

    Job(List<Run> run, String status) {
        this.run = run;
        this.status = status;
    }

    public Long getJobId() {
        return jobId;
    }

    public List<Run> getRun() {
        return run;
    }

    public void setRun(List<Run> run) {
        this.run = run;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<S3File> getS3Files() {
        return s3Files;
    }

    public void setS3Files(List<S3File> s3Files) {
        this.s3Files = s3Files;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return Objects.equals(jobId, job.jobId) && Objects.equals(run, job.run) && Objects.equals(s3Files, job.s3Files) && Objects.equals(status, job.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, run, s3Files, status);
    }

    @Override
    public String toString() {
        return "Job{id=" + jobId + ", status='" + status + "'}";
    }
}