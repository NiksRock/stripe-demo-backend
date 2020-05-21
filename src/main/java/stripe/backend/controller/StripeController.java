package stripe.backend.controller;

import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stripe.backend.responseDTO.GenericResponse;
import stripe.backend.service.StripeService;

import java.util.List;

@RestController
public class StripeController {

    @Autowired
    private StripeService stripeService;

    @GetMapping("/createCustomer")
    @ResponseBody
    public ResponseEntity<GenericResponse> createCustomerOnStripe(@RequestParam String email, @RequestParam String token) {
        return ResponseEntity.ok(stripeService.createCustomer(email, token));
    }

    @GetMapping("/attachPaymentMethodToCustomer")
    @ResponseBody
    public ResponseEntity<GenericResponse> attachPaymentMethodToCustomer(@RequestParam String id, @RequestParam String customerID) {
        return ResponseEntity.ok(stripeService.attachPaymentMethodToCustomer(id, customerID));
    }

    @PostMapping("/createPaymentMethod")
    public ResponseEntity<GenericResponse> createPaymentMethod(@RequestParam Long cardNumber, @RequestParam Integer expMonth, @RequestParam Integer expYear, @RequestParam Integer cvvNumber) {
        return ResponseEntity.ok(stripeService.createPaymentMethod(cardNumber, expMonth, expYear, cvvNumber));
    }

    @GetMapping("/getProductWithPlans")
    @ResponseBody
    public ResponseEntity<GenericResponse> getProductWithPlans() {
        return ResponseEntity.ok(stripeService.getProductWithPlans());
    }

    @PostMapping("/createProduct")
    public ResponseEntity<GenericResponse> createProduct(String productName) {
        return ResponseEntity.ok(stripeService.createProduct(productName));
    }

    @PostMapping("/createPlanOfProduct")
    public ResponseEntity<GenericResponse> createPlanOfProduct(String productId, String currency, String interval, Long amount) {
        return ResponseEntity.ok(stripeService.createPlanOfProduct(productId, currency, interval, amount));
    }

    @PostMapping("/createCoupon")
    public ResponseEntity<GenericResponse> createCoupon(Integer percentageOff, String duration, Integer durationInMonth) {
        return ResponseEntity.ok(stripeService.couponCreate(percentageOff, duration, durationInMonth));
    }

    @DeleteMapping("/cancelSubscription")
    public ResponseEntity<GenericResponse> cancelSubscription(String subscriptionId) {
        return ResponseEntity.ok(stripeService.cancelSubscription(subscriptionId));
    }

    @GetMapping("/retrieveSubscription")
    public ResponseEntity<GenericResponse> retrieveSubscription(String subscriptionId) {
        return ResponseEntity.ok(stripeService.retrieveSubscriptionStatus(subscriptionId));
    }

    @GetMapping("/retrieveAllPlan")
    public String retrieveAllPlan() {
        return stripeService.retrieveAllPlan();
    }

    @GetMapping("/retrieveAllCoupons")
    public String retrieveAllCoupons() {
        return stripeService.retrieveAllCoupons();
    }

    @GetMapping("/retrieveAllProducts")
    public String retrieveAllProducts() {
        return stripeService.retrieveAllProducts();
    }

    @PostMapping("/createSubscription")
    public String createSubscription(@RequestParam String customerID, @RequestParam String planId) {
        return stripeService.createSubscription(customerID, planId);
    }

    @PostMapping("/chargeToCustomer")
    public String chargeToCustomer(@RequestParam String customerId, @RequestParam Long amount, @RequestParam String currency, @RequestParam String paymentId) {
        return stripeService.chargeCustomer(customerId, amount, currency, paymentId);
    }

    @PostMapping("/endPoint")
    public String endPoint() {
        System.out.println("End Point Called");
        return "End_Point Called";
    }

    @GetMapping("/oneTimePayment")
    public String oneTimePayment(String paymentMethodId, String customerId) {
        return stripeService.secure3DPayment(paymentMethodId, customerId);
    }

    @GetMapping("/getSubscriptionByEmail")
    public String getSubscriptionByEmail(@RequestParam String email) {
        return stripeService.retrieveSubscriptionByEmail(email);
    }

    @GetMapping("/retrieveAllCardOfCustomerById")
    public String retrieveAllCardOfCustomerById(String customerId) throws StripeException {
        return stripeService.retrieveAllCardOfCustomerById(customerId);
    }
}
