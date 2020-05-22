package stripe.backend.service;

import com.stripe.exception.StripeException;
import stripe.backend.responseDTO.GenericResponse;

import java.util.List;
import java.util.Map;

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

    public String paymentIntent(Long amount);

    public String secure3DPayment(String paymentMethodId, String customerId);

    public String retrieveSubscriptionByEmail(String email);

    public String retrieveAllCardOfCustomerById(String customerId) throws StripeException;
}
