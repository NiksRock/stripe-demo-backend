package stripe.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import stripe.backend.model.UserBilling;

@Repository
public interface UserBillingRepo extends JpaRepository<UserBilling, Long> {
}
