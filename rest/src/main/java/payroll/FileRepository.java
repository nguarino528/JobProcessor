package payroll;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import payroll.entity.S3File;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<S3File, UUID> {
    Optional<S3File> findByS3Key(String s3Key);
    List<S3File> findByJobJobId(Long jobId);
}