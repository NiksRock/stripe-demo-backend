package stripe.backend.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import stripe.backend.model.PassportPlans;
import stripe.backend.repo.PassportPlansRepo;
import stripe.backend.responseDTO.APIResponseBuilder;
import stripe.backend.responseDTO.GenericResponse;
import stripe.backend.service.PassportPlansService;

import java.util.List;

@Service
public class PassportPlansServiceImpl implements PassportPlansService {

    @Autowired
    private PassportPlansRepo passportPlansRepo;

    @Override
    public GenericResponse findAllPassportPlans() {
        try {
            List<PassportPlans> passportPlansList = passportPlansRepo.findAll();
            return APIResponseBuilder.build(true, passportPlansList, passportPlansList.size()<=0 ? "No record found" : "find record successfully" );
        } catch (Exception e) {
            return APIResponseBuilder.build(false, e.getMessage(), "While fetching all passport plan");
        }
    }
}
