package payroll;

import org.springframework.data.jpa.repository.JpaRepository;

interface JobRepository extends JpaRepository<Job, Long> {

}
