package payroll;

import org.springframework.data.jpa.repository.JpaRepository;
import payroll.entity.Job;
import payroll.entity.Run;

import java.util.Optional;

interface JobRepository extends JpaRepository<Job, Long> {

}
