package stripe.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import stripe.backend.model.SubscriptionBilling;

@Repository
public interface SubscriptionRepo extends JpaRepository<SubscriptionBilling, Long> {

    SubscriptionBilling findByMembers(Long memberId);

    SubscriptionBilling findByCustomerIdAndStatus(String customerId, String status);

    SubscriptionBilling findByStripeSubscriptionId(String stripeSubscriptionId);

    @Query("from SubscriptionBilling s JOIN s.members m WHERE m.email=:email AND s.status=:status")
    SubscriptionBilling findSubscriptionIdByEmail(String email, String status);
}
