package stripe.backend.service;

import java.util.Map;

import com.stripe.model.Product;

import stripe.backend.responseDTO.GenericResponse;

public interface StripeService {

    GenericResponse createCustomer(String email, String token);

	GenericResponse attachPaymentMethodToCustomer(String id, String customerID);

	GenericResponse getProductWithPlans();

	Map<String, Object> getProductByID(String id);

    GenericResponse createProduct(String productName);

    GenericResponse createPlanOfProduct(String productId, String currency, String interval, Long amount);

    GenericResponse couponCreate(Integer percentageOff, String duration, Integer durationInMonth);

    GenericResponse cancelSubscription(String subscriptionId);

    GenericResponse retrieveSubscriptionStatus(String subscriptionId);

    GenericResponse customerPayment(String email, Long cardNumber, Integer expMonth, Integer expYear, Integer cvvNumber, String plan, String coupon);

    public String retrieveAllPlan();

    public String retrieveAllCoupons();

    public String retrieveAllProducts();

    GenericResponse  createSubscription(String customerId, String plan, String coupon);
}
