package payroll;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

@Entity
class Job {

    @Id
    @GeneratedValue
    private Long jobId;

    @OneToMany(mappedBy = "job", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Run> run;

    private long duration;
    private String status;

    Job() {}

    Job(List<Run> run, long duration, String status) {
        this.run = run;
        this.duration = duration;
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

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
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
        Job job = (Job) o;
        return duration == job.duration && Objects.equals(jobId, job.jobId) && Objects.equals(run, job.run) && Objects.equals(status, job.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, run, duration, status);
    }

    @Override
    public String toString() {
        return "Job{id=" + jobId + ", duration=" + duration + ", status='" + status + "'}";
    }
}