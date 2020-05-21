package stripe.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import stripe.backend.model.Members;

@Repository
public interface MembersRepo extends JpaRepository<Members, Long> {

    Members findByEmail(String email);
}
