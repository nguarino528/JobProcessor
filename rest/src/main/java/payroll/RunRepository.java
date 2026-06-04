package payroll;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import payroll.entity.Run;

@Repository
public interface RunRepository extends JpaRepository<Run, UUID> {
    Optional<Run> findFirstByJob_JobIdAndStatusOrderByCreatedAtDesc(Long jobId, String status);
}