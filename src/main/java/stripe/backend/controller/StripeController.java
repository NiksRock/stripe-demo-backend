package stripe.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import stripe.backend.responseDTO.GenericResponse;
import stripe.backend.service.StripeService;

@RestController
public class StripeController {

    @Autowired
    private StripeService stripeService;

    @GetMapping("/createCustomer")
    @ResponseBody
    public ResponseEntity<GenericResponse> createCustomerOnStripe(@RequestParam String email,@RequestParam String token) {
        return ResponseEntity.ok(stripeService.createCustomer(email,token));
    }
    @GetMapping("/attachPaymentMethodToCustomer")
    @ResponseBody
    public  ResponseEntity<GenericResponse>  attachPaymentMethodToCustomer(@RequestParam String id,@RequestParam String customerID) {
    	return ResponseEntity.ok(stripeService.attachPaymentMethodToCustomer(id,customerID));
    }
    @GetMapping("/getProductWithPlans")
    @ResponseBody
    public  ResponseEntity<GenericResponse>  getProductWithPlans() {
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

    @PostMapping("/customerPayment")
    public ResponseEntity<GenericResponse> customerPayment(String email, Long cardNumber, Integer expMonth, Integer expYear, Integer cvvNumber, String plan, String coupon) {
        return ResponseEntity.ok(stripeService.customerPayment(email, cardNumber, expMonth, expYear, cvvNumber, plan, coupon));
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
    @GetMapping("/createSubscription")
    public ResponseEntity<GenericResponse>  createSubscription(@RequestParam String customerID,@RequestParam String planId,@RequestParam String coupon) {
    	 return ResponseEntity.ok(stripeService.createSubscription(customerID, planId, coupon));
    }
    
}
