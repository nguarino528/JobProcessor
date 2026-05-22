package payroll;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.regions.Region;

@Configuration
class LoadDatabase {

	private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

	@Bean
	CommandLineRunner initDatabase(JobRepository repository) {

		return args -> {
			//log.info("Preloading " + repository.save(new Job(100, "PENDING")));
			//log.info("Preloading " + repository.save(new Job(1000, "PENDING")));
		};
	}

    @Bean
    SqsClient sqsClient(){
        return SqsClient.builder()
                .region(Region.US_EAST_1) // change to your region
                .build();
    }
}
