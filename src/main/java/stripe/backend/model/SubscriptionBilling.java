package stripe.backend.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "subscription_billing")
public @Data
class SubscriptionBilling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @ManyToOne
    @JoinColumn(name = "memberId", referencedColumnName = "id")
    private Members members;

    @Column(unique = true, nullable = false, updatable = false)
    private String stripeSubscriptionId;

    @Column
    private String customerId;

    @Column
    private String status;

    @Column
    private Date createdDate;

    @Column
    private Date endDate;

    @Column
    private Date canceledDated;

    @Column
    private double amount;

    @Column
    private String paymentStatus;

    @ManyToOne
    @JoinColumn(name = "passportPlanId")
    private PassportPlans passportPlans;

    @Column
    private String stripePlanId;

    @Column
    private Boolean cancelAtPeriodEnd;

    @Column
    private Date reActivatedDate;


}
