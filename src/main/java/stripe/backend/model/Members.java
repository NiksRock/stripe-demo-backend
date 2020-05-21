package stripe.backend.model;

import lombok.*;

import javax.persistence.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "members")
public @Data
class Members {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String email;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private String customerId;
}
