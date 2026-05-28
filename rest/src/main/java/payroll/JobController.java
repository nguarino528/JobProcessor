package payroll;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import message.RunMessage;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.annotations.NotNull;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

// tag::hateoas-imports[]
// end::hateoas-imports[]

@RestController
class JobController {

	private final JobRepository jobRepository;
    private final RunRepository runRepository;
    private final SqsClient sqsClient;
    static String queueUrl = "https://sqs.us-east-1.amazonaws.com/646053151564/JobQeue";
	JobController(JobRepository jobRepository, RunRepository runRepository, SqsClient sqsClient) {
		this.jobRepository = jobRepository;
        this.runRepository = runRepository;
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
    public Job createJob(@RequestBody Job newJob) throws Exception{
        Job savedJob = jobRepository.save(newJob);

        // 2) Create and link run to persisted job
        Run run = new Run("CREATED");
        run.setJob(savedJob);

        // 3) Save run (owning side)
        Run savedRun = runRepository.save(run);

        // 4) Keep inverse side in sync if you expose it in Job
        savedJob.setRun(List.of(savedRun));
        RunMessage runMessage = new RunMessage(run.getRunId(), savedJob.getJobId(), run.getStatus());
        ObjectMapper objectMapper = new ObjectMapper();

        String runJson = objectMapper.writeValueAsString(runMessage);
        sqsClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(runJson)
                        .build());

        return savedJob;
    }
    @GetMapping("/jobs/result/{jobId}")
    public Run getLatestSuccessfulRun(@PathVariable Long jobId) {
        Optional<Run> run = runRepository.findFirstByJob_JobIdAndStatusOrderByCreatedAtDesc(jobId,"done");
        if(run.isPresent()){
            return run.get();
        }
        return null;
    }
    @PostMapping("/run")
    public RunMessage createRun(@RequestBody CreateRunRequest request) throws Exception {
        Run run = new Run("CREATED");
        run.setJob(jobRepository.getReferenceById(request.getJobId()));

        // 3) Save run (owning side)
        Run savedRun = runRepository.save(run);
        RunMessage runMessage = new RunMessage(run.getRunId(), run.getJob().getJobId(), run.getStatus());
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
					job.setDuration(newJob.getDuration());;
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