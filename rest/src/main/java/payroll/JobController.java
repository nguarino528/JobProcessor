package payroll;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import message.CompletedRunMessage;
import message.CreateJobRequest;
import message.PresignUploadRequest;
import message.RunMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import payroll.entity.Job;
import payroll.entity.Run;
import payroll.entity.S3File;
import software.amazon.awssdk.annotations.NotNull;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

// tag::hateoas-imports[]
// end::hateoas-imports[]

@RestController
class JobController {

	private final JobRepository jobRepository;
    private final RunRepository runRepository;
    private final FileRepository fileRepository;
    private final S3Presigner s3Presigner;
    private final SqsClient sqsClient;

    @Value("${app.s3.outputbucket}")
    private String bucket;
    static String queueUrl = "https://sqs.us-east-1.amazonaws.com/646053151564/JobQeue";
	JobController(JobRepository jobRepository, RunRepository runRepository, FileRepository fileRepository, S3Presigner s3Presigner, SqsClient sqsClient) {
		this.jobRepository = jobRepository;
        this.runRepository = runRepository;
        this.fileRepository = fileRepository;
        this.s3Presigner = s3Presigner;
        this.sqsClient = sqsClient;
	}

	// Aggregate root
    @GetMapping("/health")
    String health() {

        return "Healthy";
    }
	// tag::get-aggregate-root[]
	@GetMapping("/jobs")
	CollectionModel<EntityModel<Job>> all() {

		List<EntityModel<Job>> jobs = jobRepository.findAll().stream()
				.map(job -> EntityModel.of(job,
						linkTo(methodOn(JobController.class).one(job.getJobId())).withSelfRel(),
						linkTo(methodOn(JobController.class).all()).withRel("jobs")))
				.collect(Collectors.toList());

		return CollectionModel.of(jobs, linkTo(methodOn(JobController.class).all()).withSelfRel());
	}
	// end::get-aggregate-root[]
    @PostMapping("/jobs")
    @Transactional
    public Job createJob(@RequestBody CreateJobRequest request) throws Exception{
        Job job = new Job();
        S3File file = fileRepository.getReferenceById(UUID.fromString(request.fileUUID()));
        job.setStatus("CREATED");
        Job j = jobRepository.save(job);
        file.setJob(j);
        fileRepository.save(file);
        return j;
    }
    @GetMapping("/jobs/result/{jobId}")
    public CompletedRunMessage getLatestSuccessfulRun(@PathVariable Long jobId) {
        Optional<Run> optionalRun = runRepository.findFirstByJob_JobIdAndStatusOrderByCreatedAtDesc(jobId,"DONE");

        if(!optionalRun.isPresent()){
            return null;
        }

        Run run = optionalRun.get();
        String filename = "result.png";

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(run.getS3OutputKey())
                .responseContentDisposition(
                        "attachment; filename=\"" + filename + "\"")
                .build();
        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(5))
                        .getObjectRequest(getObjectRequest)
                        .build();

        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(presignRequest);
        return new CompletedRunMessage(presignedRequest.url().toString(),run.getRunId().toString(),run.getStatus());
    }
    @PostMapping("/run")
    public RunMessage createRun(@RequestBody CreateRunRequest request) throws Exception {
        Run run = new Run("CREATED");
        Job job = jobRepository.getReferenceById(request.getJobId());
        run.setJob(job);
        List<S3File> files = fileRepository.findByJobJobId(job.getJobId());

        // 3) Save run (owning side)
        Run savedRun = runRepository.save(run);
        RunMessage runMessage = new RunMessage(run.getRunId(), run.getJob().getJobId(), run.getStatus(), files.stream().map(S3File::getS3Key).collect(Collectors.toList()));
        ObjectMapper objectMapper = new ObjectMapper();

        String runJson = objectMapper.writeValueAsString(runMessage);
        sqsClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(runJson)
                        .build());
        return runMessage;
    }
	// Single item

	// tag::get-single-item[]
	@GetMapping("/jobs/{id}")
	EntityModel<Job> one(@PathVariable Long id) {

		Job job = jobRepository.findById(id) //
				.orElseThrow(() -> new EmployeeNotFoundException(id));

		return EntityModel.of(job, //
				linkTo(methodOn(JobController.class).one(id)).withSelfRel(),
				linkTo(methodOn(JobController.class).all()).withRel("employees"));
	}
	// end::get-single-item[]

	@PutMapping("/jobs/{id}")
    Job replaceEmployee(@RequestBody Job newJob, @PathVariable Long id) {

		return jobRepository.findById(id) //
				.map(job -> {
					return jobRepository.save(job);
				}) //
				.orElseGet(() -> {
					return jobRepository.save(newJob);
				});
	}

	@DeleteMapping("/jobs/{id}")
	void deleteEmployee(@PathVariable Long id) {
		jobRepository.deleteById(id);
	}
}

class CreateRunRequest {

    @NotNull
    private Long jobId;

    public CreateRunRequest() {
    }

    public CreateRunRequest(Long jobId) {
        this.jobId = jobId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }
}