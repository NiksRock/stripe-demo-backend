package stripe.backend.serviceImpl;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Coupon;
import com.stripe.model.CouponCollection;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentSourceCollection;
import com.stripe.model.Plan;
import com.stripe.model.PlanCollection;
import com.stripe.model.Product;
import com.stripe.model.ProductCollection;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.checkout.SessionCreateParams.SubscriptionData;
import com.stripe.param.checkout.SessionCreateParams.SubscriptionData.Item;

import org.springframework.web.client.RestTemplate;
import stripe.backend.model.Members;
import stripe.backend.model.SubscriptionBilling;
import stripe.backend.repo.MembersRepo;
import stripe.backend.repo.SubscriptionRepo;
import stripe.backend.responseDTO.APIResponseBuilder;
import stripe.backend.responseDTO.GenericResponse;
import stripe.backend.service.StripeService;

import javax.servlet.http.HttpServletRequest;

@Service
public class StripeServiceImpl implements StripeService {

    @Value("${stripe.keys.secret}")
    private String API_SECRET_KEY;

    @Autowired
    private MembersRepo membersRepo;

    @Autowired
    private SubscriptionRepo subscriptionRepo;

    private final String REDIRECT_URL = "http://localhost:3000";

    @Override
    public GenericResponse createCustomer(String email, String paymentMethodId) {
        try {
            if (email != null) {
                Members members = membersRepo.findByEmail(email);
                if (members != null) {
                    Stripe.apiKey = API_SECRET_KEY;
                    if (members.getCustomerId() != null && !members.getCustomerId().isEmpty()) {
                        // update customer unique id here to track them in your web application
                        //update a exist customer
                        CustomerUpdateParams params =
                                CustomerUpdateParams.builder()
                                        .setEmail(email)
                                        .setName(members.getName())
                                        .setDescription("Customer for" + email)
                                        .setPhone(members.getPhoneNumber().toString())
                                        .setAddress(CustomerUpdateParams.Address.builder()
                                                        .setLine1(members.getAddress())
                                                        .setPostalCode(members.getPostalCode().toString())
                                                        .setCountry("US")
                                                        .build())
                                        .build();
                        Customer customer = Customer.retrieve(members.getCustomerId());
                        Customer updateCustomer = customer.update(params);
                        PaymentMethod pm = PaymentMethod.retrieve(paymentMethodId);
                        pm.attach(PaymentMethodAttachParams.builder().setCustomer(updateCustomer.getId()).build());
                        return APIResponseBuilder.build(true, updateCustomer.getId(), "Customer updated successfully");
                    } else {
                        // add customer unique id here to track them in your web application
                        CustomerCreateParams params =
                                CustomerCreateParams.builder()
                                        .setEmail(email)
                                        .setName(members.getName())
                                        .setPaymentMethod(paymentMethodId)
                                        .setDescription("Customer for " + email)
                                        .setPhone(members.getPhoneNumber().toString())
                                        .setAddress(
                                                CustomerCreateParams.Address.builder()
                                                        .setLine1(members.getAddress())
                                                        .setPostalCode(members.getPostalCode().toString())
                                                        .setCountry("US")
                                                        .build())
                                        .setInvoiceSettings(
                                                CustomerCreateParams.InvoiceSettings.builder()
                                                        .setDefaultPaymentMethod(paymentMethodId)
                                                        .build())
                                        .build();
                        Customer newCustomer = Customer.create(params);
                        members.setCustomerId(newCustomer.getId());
                        membersRepo.save(members);
                        return APIResponseBuilder.build(true, newCustomer.getId(), "Customer created successfully");
                    }
                } else {
                    return APIResponseBuilder.build(false, email, "This email does not exits");
                }
            } else {
                return APIResponseBuilder.build(false, email, "Email is required");
            }
        } catch (Exception ex) {
            return APIResponseBuilder.build(false, ex.getMessage(), "while creating customer");
        }
    }

    @Override
    public GenericResponse attachPaymentMethodToCustomer(String id, String customerID) {
        Stripe.apiKey = API_SECRET_KEY;
        try {

            PaymentMethod paymentMethod = PaymentMethod.retrieve(id);

            Map<String, Object> params = new HashMap<>();
            params.put("customer", customerID);
            paymentMethod = paymentMethod.attach(params);
            return APIResponseBuilder.build(true, paymentMethod.getCustomer(),
                    "attach Payment Method To Customer successfully");

        } catch (Exception e) {
            return APIResponseBuilder.build(false, e.getMessage(), "while attaching Payment Method To Customer ");
        }
    }

    @Override
    public GenericResponse getProductWithPlans() {
        Stripe.apiKey = API_SECRET_KEY;
        try {
            Map<String, Object> productWithPlans = new HashMap<>();
            PlanCollection plans = Plan.list(new HashMap<>());
            for (int i = 0; i < plans.getData().size(); i++) {
                if (productWithPlans.containsKey(plans.getData().get(i).getProduct())) {
                    Map<String, Object> productDetail = (Map<String, Object>) productWithPlans
                            .get(plans.getData().get(i).getProduct());

                    Map<String, Object> tempPlan = new HashMap<>();
                    tempPlan.put("active", plans.getData().get(i).getActive());
                    tempPlan.put("amount", plans.getData().get(i).getAmount());
                    tempPlan.put("billingScheme", plans.getData().get(i).getBillingScheme());
                    tempPlan.put("currency", plans.getData().get(i).getCurrency());
                    tempPlan.put("id", plans.getData().get(i).getId());
                    tempPlan.put("interval", plans.getData().get(i).getInterval());
                    tempPlan.put("intervalCount", plans.getData().get(i).getIntervalCount());
                    tempPlan.put("nickname", plans.getData().get(i).getNickname());

                    ArrayList<Map<String, Object>> planList = (ArrayList<Map<String, Object>>) productDetail
                            .get("plans");
                    planList.add(tempPlan);
                    productDetail.put("plans", planList);
                } else {
                    productWithPlans.put(plans.getData().get(i).getProduct(),
                            getProductByID(plans.getData().get(i).getProduct()));
                    Map<String, Object> productDetail = (Map<String, Object>) productWithPlans
                            .get(plans.getData().get(i).getProduct());

                    Map<String, Object> tempPlan = new HashMap<>();
                    tempPlan.put("active", plans.getData().get(i).getActive());
                    tempPlan.put("amount", plans.getData().get(i).getAmount());
                    tempPlan.put("billingScheme", plans.getData().get(i).getBillingScheme());
                    tempPlan.put("currency", plans.getData().get(i).getCurrency());
                    tempPlan.put("id", plans.getData().get(i).getId());
                    tempPlan.put("interval", plans.getData().get(i).getInterval());
                    tempPlan.put("intervalCount", plans.getData().get(i).getIntervalCount());
                    tempPlan.put("nickname", plans.getData().get(i).getNickname());

                    ArrayList<Map<String, Object>> planList = (ArrayList<Map<String, Object>>) productDetail
                            .get("plans");
                    planList.add(tempPlan);
                    productDetail.put("plans", planList);
                }
            }
            return APIResponseBuilder.build(true, productWithPlans, "extract product and plans successfully");
        } catch (Exception e) {
            return APIResponseBuilder.build(false, e.getMessage(), "while extracting product and  plans");
        }
    }

    @Override
    public Map<String, Object> getProductByID(String id) {
        try {
            Product product = Product.retrieve(id);
            Map<String, Object> productDetail = new HashMap<>();
            productDetail.put("isActive", product.getActive());
            productDetail.put("description", product.getDescription());
            productDetail.put("name", product.getName());
            productDetail.put("statementDescriptor", product.getStatementDescriptor());
            productDetail.put("plans", new ArrayList<>());
            return productDetail;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public GenericResponse createProduct(String productName) {
        try {
            Stripe.apiKey = API_SECRET_KEY;
            // create product
            if (productName != null && !productName.isEmpty()) {
                ProductCreateParams params = ProductCreateParams.builder().setName(productName)
                        .setType(ProductCreateParams.Type.SERVICE).build();
                Product product = Product.create(params);
                return APIResponseBuilder.build(true, product.getId(), "Product created successfully");
            } else {
                return APIResponseBuilder.build(false, productName, "Product name is required");
            }
        } catch (Exception e) {
            return APIResponseBuilder.build(false, e.getMessage(), "While creating product");
        }
    }

    @Override
    public GenericResponse createPlanOfProduct(String productId, String currency, String interval, Long amount) {
        try {
            Stripe.apiKey = API_SECRET_KEY;
            // create product of plan
            if (productId != null && !productId.isEmpty() && currency != null && !currency.isEmpty() && interval != null
                    && !interval.isEmpty() && amount != null) {
                Map<String, Object> objectMap = new HashMap<String, Object>();
                objectMap.put("amount", amount);
                objectMap.put("interval", interval);
                objectMap.put("currency", currency);
                objectMap.put("product", productId);
                Plan plan = Plan.create(objectMap);
                return APIResponseBuilder.build(true, plan.getId(), "Plan created successfully");
            } else {
                return APIResponseBuilder.build(false, productId, "All field is required");
            }
        } catch (Exception e) {
            return APIResponseBuilder.build(false, e.getMessage(), "While creating plan");
        }
    }

    @Override
    public GenericResponse couponCreate(Integer percentageOff, String duration, Integer durationInMonth) {
        try {
            Stripe.apiKey = API_SECRET_KEY;
            if (percentageOff != null && duration != null && !duration.isEmpty() && durationInMonth != null) {
                // create coupon
                Map<String, Object> params = new HashMap<>();
                params.put("percent_off", percentageOff);
                params.put("duration", duration);
                params.put("duration_in_months", durationInMonth);

                Coupon coupon = Coupon.create(params);
                return APIResponseBuilder.build(true, coupon.getId(), "Coupon created successfully");
            } else {
                return APIResponseBuilder.build(false, null, "All field is required");
            }
        } catch (Exception e) {
            return APIResponseBuilder.build(false, e.getMessage(), "While creating coupon");
        }
    }

    @Override
    public GenericResponse cancelSubscription(String subscriptionId) {
        try {
            Stripe.apiKey = API_SECRET_KEY;
            if (subscriptionId != null && !subscriptionId.isEmpty()) {
                Boolean isSubscriptionCancelTrue = this.subscriptionCancel(subscriptionId);
                if (isSubscriptionCancelTrue == true) {
                    return APIResponseBuilder.build(true, subscriptionId, "Subscription cancelled successfully");
                } else {
                    return APIResponseBuilder.build(true, subscriptionId, "Failed to cancel the subscription. Please, try later.");
                }
            } else {
                return APIResponseBuilder.build(true, null, "SubscriptionId is required");
            }
        } catch (Exception e) {
            return APIResponseBuilder.build(false, e.getMessage(), "While cancelling subscription");
        }
    }

    private Boolean subscriptionCancel(String subscriptionId) {
        boolean status;
        try {
            Stripe.apiKey = API_SECRET_KEY;
            Subscription sub = Subscription.retrieve(subscriptionId);
            sub.cancel();
            Subscription canceledSubscription = Subscription.retrieve(subscriptionId);
            SubscriptionBilling subscriptionBilling = subscriptionRepo.findByStripeSubscriptionId(subscriptionId);
            subscriptionBilling.setStatus(canceledSubscription.getStatus());
            subscriptionBilling.setCanceledDated(new Date());
            subscriptionRepo.save(subscriptionBilling);
            status = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            status = false;
        }
        return status;
    }

    @Override
    public GenericResponse retrieveSubscriptionStatus(String subscriptionId) {
        try {
            Stripe.apiKey = API_SECRET_KEY;
            if (subscriptionId != null && !subscriptionId.isEmpty()) {
                Subscription subscription = Subscription.retrieve(subscriptionId);
                return APIResponseBuilder.build(true, subscription.toJson(), "Find subscription status for this Id " + subscriptionId + " status is " + subscription.getStatus());
            } else {
                return APIResponseBuilder.build(true, null, "SubscriptionId is required");
            }
        } catch (StripeException e) {
            return APIResponseBuilder.build(false, e.getMessage(), "While fetching subscription status");
        }
    }

    @Override
    public GenericResponse createSubscription(String customerId, String plan) {
        try {
            Stripe.apiKey = API_SECRET_KEY;
            SubscriptionBilling subscriptionId = subscriptionRepo.findByCustomerIdAndStatus(customerId, "active");
            if (subscriptionId != null) {
                if (subscriptionId.getCustomerId() != null && !subscriptionId.getCustomerId().isEmpty()) {
                    Subscription subscription = Subscription.retrieve(subscriptionId.getStripeSubscriptionId());
                    if (subscription.getStatus().equalsIgnoreCase("active")) {
                    	  return APIResponseBuilder.build(true, "Your have subscribed plan and subscription status is " + subscription.getStatus(), "Your have subscribed plan and subscription");
                          
                    } else {
                    	 return APIResponseBuilder.build(true, "Your subscription package is expired! " + subscription.getStatus(),"expired");
                    }
                }
            } else {
                //create subscription
                String subId = this.subscription(customerId, plan);
                if (subId == null) {
                	return APIResponseBuilder.build(true, "An error occurred while trying to create a subscription.","");
                }
                Subscription subscription = Subscription.retrieve(subId);
                if (subscription.getStatus().equalsIgnoreCase("incomplete") && subscription.getStatus().equalsIgnoreCase("trialing")) {
                	return APIResponseBuilder.build(true,"Your payment is incomplete! please try again","");
                }
                Members members = membersRepo.findByCustomerId(customerId);
                SubscriptionBilling subscriptionBilling = new SubscriptionBilling();
                subscriptionBilling.setCustomerId(customerId);
                subscriptionBilling.setCreatedDate(new Date());
                subscriptionBilling.setStripeSubscriptionId(subId);
                subscriptionBilling.setMembers(members);
                subscriptionBilling.setAmount(subscription.getPlan().getAmount());
                subscriptionBilling.setStatus(subscription.getStatus());
                String invoiceId = subscription.getLatestInvoice();
                Invoice invoice = Invoice.retrieve(invoiceId);
                subscriptionBilling.setPaymentStatus(invoice.getStatus());
                //subscriptionBilling.setPassportPlans();
                subscriptionRepo.save(subscriptionBilling);
                return APIResponseBuilder.build(true, invoice.toJson(),"");
            }
        } catch (Exception e) {
        	return APIResponseBuilder.build(true,"An error while attached subscription to the customer","");
        }
        return null;
    }

    @Override
    public GenericResponse createPaymentMethod(Long cardNumber, Integer expMonth, Integer expYear, Integer cvvNumber) {
        try {
            Stripe.apiKey = API_SECRET_KEY;
            Map<String, Object> card = new HashMap<>();
            card.put("number", cardNumber);
            card.put("exp_month", expMonth);
            card.put("exp_year", expYear);
            card.put("cvc", cvvNumber);
            Map<String, Object> params = new HashMap<>();
            params.put("type", "card");
            params.put("card", card);

            PaymentMethod paymentMethod = PaymentMethod.create(params);
            return APIResponseBuilder.build(true, paymentMethod.getId(), "Payment method created successfully");
        } catch (Exception e) {
            return APIResponseBuilder.build(false, e.getMessage(), "An error while creating payment method");
        }
    }

    @Override
    public String retrieveAllPlan() {
        try {
            Stripe.apiKey = API_SECRET_KEY;
            Map<String, Object> params = new HashMap<>();
            params.put("limit", 8);

            PlanCollection plans = Plan.list(params);
            return plans.toJson();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String retrieveAllCoupons() {
        try {
            Stripe.apiKey = API_SECRET_KEY;
            Map<String, Object> params = new HashMap<>();
            params.put("limit", 10);

            CouponCollection coupons = Coupon.list(params);
            return coupons.toJson();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String retrieveAllProducts() {
        try {
            Stripe.apiKey = API_SECRET_KEY;
            Map<String, Object> params = new HashMap<>();
            params.put("limit", 10);

            ProductCollection products = Product.list(params);
            return products.toJson();
        } catch (Exception e) {
            return e.getMessage();
        }
    }


    private String subscription(String customerId, String plan) {
        try {
            Stripe.apiKey = API_SECRET_KEY;
            if (customerId != null && !customerId.isEmpty() && plan != null && !plan.isEmpty()) {

                Map<String, Object> item = new HashMap<>();
                item.put("plan", plan);

                Map<String, Object> items = new HashMap<>();
                items.put("0", item);

                Map<String, Object> expand = new HashMap<>();
                expand.put("0", "latest_invoice.payment_intent");

                Map<String, Object> params = new HashMap<>();
                params.put("customer", customerId);
                params.put("items", items);
                params.put("expand", expand);

//                //add coupon if available
//                if (!coupon.isEmpty()) {
//                    params.put("coupon", coupon);
//                }
                Subscription sub = Subscription.create(params);
                return sub.getId();
            } else {
                return "customerId and planId is required";
            }
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    @Override
    public String paymentIntent(Long amount) {
        try {
            Stripe.apiKey = API_SECRET_KEY;
            if (amount != null) {
                List<Object> paymentMethodTypes = new ArrayList<>();
                paymentMethodTypes.add("card");
                Map<String, Object> params = new HashMap<>();
                params.put("amount", amount);
                params.put("currency", "inr");
                params.put("payment_method_types", paymentMethodTypes);
                PaymentIntent paymentIntent = PaymentIntent.create(params);
                return paymentIntent.getClientSecret();
            } else {
                return "Amount is required";
            }
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String secure3DPayment(String paymentMethodId, String customerId) {
        PaymentIntent intent = null;
        Gson gson = new Gson();
        String email = "techavidus9@gmail.com";
        try {
            if (paymentMethodId != null) {
                PaymentIntentCreateParams createParams = PaymentIntentCreateParams.builder()
                        .setAmount(4900L)
                        .setCurrency("inr")
                        .setReceiptEmail(email)
                        .setDescription("Event Payment")
                        .setOffSession(true)
                        .setConfirm(true)
                        //   .setCustomer(customerId)
                        .setPaymentMethod(paymentMethodId)
                        .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
                        .build();
                intent = PaymentIntent.create(createParams);
            } else if (intent.getId() != null) {
                intent = PaymentIntent.retrieve(intent.getId());
                intent = intent.confirm();
            }
            return intent.toJson();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return gson.toJson(errorResponse);
        }
    }

    @Override
    public GenericResponse retrieveSubscriptionByEmail(String email) {
        try {
            Stripe.apiKey = API_SECRET_KEY;
            SubscriptionBilling subscriptionBilling = subscriptionRepo.findSubscriptionIdByEmail(email, "active");
            if (subscriptionBilling != null) {
                Subscription subscription = Subscription.retrieve(subscriptionBilling.getStripeSubscriptionId());
                return APIResponseBuilder.build(true, subscription.toJson(), "Your have subscribed plan");
            } else {
            	 return APIResponseBuilder.build(false, "This " + email + " is doesn't exist", "");
            }
        } catch (StripeException e) {
       	 return APIResponseBuilder.build(false, e.getMessage(), "");
        }
    }

    @Override
    public String retrieveAllCardOfCustomerById(String customerId) throws StripeException {
        Stripe.apiKey = API_SECRET_KEY;
        Customer customer = Customer.retrieve(customerId);
        Map<String, Object> params = new HashMap<>();
        params.put("object", "card");
        params.put("limit", 10);
        PaymentSourceCollection cards = customer.getSources().list(params);
        return cards.toJson();
    }

	@Override
	public GenericResponse createCheckoutSession(String planID) {
		try {
			Stripe.apiKey = API_SECRET_KEY;
			 Map<String, Object> params = new HashMap<String, Object>();
			 ArrayList<String> paymentMethodTypes = new ArrayList<>();
			 paymentMethodTypes.add("card");
			 params.put("payment_method_types", paymentMethodTypes);
			 ArrayList<Object> itelList = new ArrayList<>();
			 Map<String, Object> item = new HashMap<String, Object>();
			 item.put("plan",planID);
			 itelList.add(item);
			 Map<String, Object> items = new HashMap<String, Object>();
			 items.put("items",itelList);
			 params.put("subscription_data",items);	
			 params.put("success_url", "http://localhost:3000/success?session_id={CHECKOUT_SESSION_ID}");
			 params.put("cancel_url", "http://localhost:3000/cancel");
			 Session session = Session.create(params);
			 return APIResponseBuilder.build(true, session.getId(), "Your have subscribed plan");
		} catch (Exception e) {
			return APIResponseBuilder.build(false, e.getMessage(), "Your have subscribed plan");
		}
		 
	}

    @Override
    public GenericResponse getCustomerAndSubscriptionDetails(String sessionId) {
        try {
            Stripe.apiKey = API_SECRET_KEY;
            Session session = Session.retrieve(sessionId);
            Customer customer = session.getCustomerObject();
            System.out.println(customer);
            Subscription subscription = session.getSubscriptionObject();
            String invoiceId = subscription.getLatestInvoice();
            Invoice invoice = Invoice.retrieve(invoiceId);
            // redirect url
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> request = new HttpEntity<String>(headers);
            restTemplate.exchange(REDIRECT_URL, HttpMethod.GET, request, String.class);
            return APIResponseBuilder.build(true, invoice.toJson(), "Get Invoice successfully");
        } catch (StripeException e) {
            return APIResponseBuilder.build(false, e.getMessage(), "While getting customer and subscription detail");
        }
    }

    @Override
    public GenericResponse cancel() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> request = new HttpEntity<String>(headers);
            restTemplate.exchange(REDIRECT_URL, HttpMethod.GET, request, String.class);
            return APIResponseBuilder.build(true, "You have cancel request");
        } catch (Exception e) {
            return APIResponseBuilder.build(false, e.getMessage(), "Exception");
        }
    }

}
