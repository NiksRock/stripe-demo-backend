package stripe.backend.serviceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.model.Coupon;
import com.stripe.model.CouponCollection;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.Plan;
import com.stripe.model.PlanCollection;
import com.stripe.model.Product;
import com.stripe.model.ProductCollection;
import com.stripe.model.Subscription;
import com.stripe.param.ProductCreateParams;

import stripe.backend.model.Members;
import stripe.backend.repo.MembersRepo;
import stripe.backend.responseDTO.APIResponseBuilder;
import stripe.backend.responseDTO.GenericResponse;
import stripe.backend.service.StripeService;

@Service
public class StripeServiceImpl implements StripeService {

	@Value("${stripe.keys.secret}")
	private String API_SECRET_KEY;

	@Autowired
	private MembersRepo membersRepo;

	@Override
	public GenericResponse createCustomer(String email ,String token) {
		try {
			Members members = membersRepo.findByEmail(email);
			Stripe.apiKey = API_SECRET_KEY;
			Map<String, Object> customerParams = new HashMap<>();
			// add customer unique id here to track them in your web application
			customerParams.put("description", "Customer for " + email);
			customerParams.put("email", email);
			customerParams.put("source", token);
			customerParams.put("name", members.getName());

			// customerParams.put("source", token); // ^ obtained with Stripe.js
			// create a new customer
			Customer customer = Customer.create(customerParams);
			String id = customer.getId();

			members.setCustomerId(id);
			membersRepo.save(members);
			System.out.println(id);
			return APIResponseBuilder.build(true, customer.getId(), "Customer created successfully");
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
			return APIResponseBuilder.build(true, productWithPlans, "extrect product and plans successfully");
		} catch (Exception e) {
			return APIResponseBuilder.build(false, e.getMessage(), "while extrecting product and  plans");
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
					return APIResponseBuilder.build(true, subscriptionId,
							"Failed to cancel the subscription. Please, try later.");
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
				return APIResponseBuilder.build(true, subscription.getStatus(), "Find subscription status for this Id "
						+ subscriptionId + " status is " + subscription.getStatus());
			} else {
				return APIResponseBuilder.build(true, null, "SubscriptionId is required");
			}
		} catch (Exception e) {
			return APIResponseBuilder.build(false, e.getMessage(), "While fetching subscription status");
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

	@Override
	public GenericResponse customerPayment(String email, Long cardNumber, Integer expMonth, Integer expYear,
			Integer cvvNumber, String plan, String coupon) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GenericResponse createSubscription(String customerId, String planId, String coupon) {
		 Stripe.apiKey = API_SECRET_KEY;
			try {
				 List<Object> items = new ArrayList<>();
				 Map<String, Object> item1 = new HashMap<>();
				 item1.put("plan",planId);
				 items.add(item1);
				 Map<String, Object> params = new HashMap<>();
				 params.put("customer", customerId);
//				 params.put("default_source",this.getCustomerPaymentMethods(customerId));
				 params.put("items", items);

				 Subscription subscription = Subscription.create(params);
				return APIResponseBuilder.build(true, subscription.getId(),
						"attach subscription To Customer successfully");

			} catch (Exception e) {
				return APIResponseBuilder.build(false, e.getMessage(), "while attaching subscription To Customer ");
			}
	}

	public String getCustomerPaymentMethods(String customerId) {

		Stripe.apiKey = API_SECRET_KEY;
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("customer", customerId);
			params.put("type", "card");

			PaymentMethodCollection paymentMethods = PaymentMethod.list(params);
			return paymentMethods.getData().get(0).getId();
		} catch (Exception e) {
			return null;
		}

	}

}
