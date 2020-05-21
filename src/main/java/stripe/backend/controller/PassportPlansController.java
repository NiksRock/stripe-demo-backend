package stripe.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stripe.backend.responseDTO.GenericResponse;
import stripe.backend.service.PassportPlansService;

@RestController
@RequestMapping("/passportPlans")
public class PassportPlansController {

    @Autowired
    private PassportPlansService passportPlansService;

    @GetMapping
    public ResponseEntity<GenericResponse> findAllPassportPlan() {
        return ResponseEntity.ok(passportPlansService.findAllPassportPlans());
    }
}
