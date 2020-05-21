package stripe.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import stripe.backend.model.PassportPlans;

@Repository
public interface PassportPlansRepo extends JpaRepository<PassportPlans, Long> {
}
