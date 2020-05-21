package stripe.backend.service;

import java.util.Map;

import com.stripe.model.Product;

import io.swagger.models.Response;
import stripe.backend.responseDTO.GenericResponse;

import javax.persistence.GeneratedValue;

public interface StripeService {

    GenericResponse createCustomer(String email, String paymentMethodId);

	GenericResponse attachPaymentMethodToCustomer(String id, String customerID);

	GenericResponse getProductWithPlans();

	Map<String, Object> getProductByID(String id);

    GenericResponse createProduct(String productName);

    GenericResponse createPlanOfProduct(String productId, String currency, String interval, Long amount);

    GenericResponse couponCreate(Integer percentageOff, String duration, Integer durationInMonth);

    GenericResponse cancelSubscription(String subscriptionId);

    GenericResponse retrieveSubscriptionStatus(String subscriptionId);

    String createSubscription(String customerId, String plan);

    GenericResponse createPaymentMethod(Long cardNumber, Integer expMonth, Integer expYear, Integer cvvNumber);

    public String retrieveAllPlan();

    public String retrieveAllCoupons();

    public String retrieveAllProducts();

    public String chargeCustomer(String customerId, Long amount, String currency, String paymentId);

    public String secure3DPayment(String paymentMethodId, String customerId);
}
