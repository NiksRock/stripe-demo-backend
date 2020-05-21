package stripe.backend.model;

import lombok.*;

import javax.persistence.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "passport_plans")
public @Data
class PassportPlans {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long passportPlanId;

    @Column
    private String name;

    @Column
    private Billing billing;

    @Column
    private double price;

    @Column
    private boolean privateMessageUsers;

    @Column
    private boolean saveLeads;

    @Column
    private boolean accessVideoLibrary;

    @Column
    private Integer maxFreeEventsPerMonth;

    @Column
    private boolean unLimitedFreeEventsPerMonth;

    @Column
    private boolean isEnabled;

    @Column
    private boolean superNetworkBadge;

    @Column
    private boolean monthlyNetworkingSession;
}

