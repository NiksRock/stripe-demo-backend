package stripe.backend.service;

import stripe.backend.responseDTO.GenericResponse;

import javax.persistence.GeneratedValue;

public interface PassportPlansService {

    GenericResponse findAllPassportPlans();
}
