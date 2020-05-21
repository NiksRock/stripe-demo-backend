package stripe.backend.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "user_billing")
public @Data
class UserBilling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long billingId;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Members members;

    @Column
    private Date billingDatetime;

    @ManyToOne
    @JoinColumn(name = "passportPlanId")
    private PassportPlans passportPlans;

    @Column
    private double amount;

    @Column
    private String customerId;
}
