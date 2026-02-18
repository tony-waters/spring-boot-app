package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer_profile")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile extends BaseEntity {

    @Id
    @Getter  // no setter by design
    private Long id;

    @Getter  // no setter by design
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    @Getter  // no setter by design
    @NotBlank
    @Size(min = 2, max = 80)
    @Column(name = "display_name", length = 80, nullable = false, unique = true)
    private String displayName;

    @Getter  // no setter by design
    @Column(name = "marketing_opt_in", nullable = false)
    private boolean marketingOptIn = false;

    // ---- Constructors ----

    Profile(String displayName, boolean marketingOptIn) {
        if(displayName == null || displayName.isBlank()) throw new IllegalArgumentException("Display name must not be blank");
        this.displayName = displayName;
        this.marketingOptIn = marketingOptIn;
    }

    // ---- Getters ----

    // ---- Domain logic - Maintain relationship invariants: Profile -> Customer ----

        // public access to relationship handled by Customer entity

    void setCustomerInternal(Customer customer) {
        if (customer == null) throw new IllegalArgumentException("Profile must have a Customer");
        if (this.customer != null && !this.customer.equals(customer)) {
            throw new IllegalStateException("Profile cannot be moved to another Customer");
        }
        this.customer = customer;
    }

    void clearCustomerInternal() {
        this.customer = null;
    }

    // ---- Domain logic - Maintain state transition invariants ----

    public void changeDisplayName(String newDisplayName) {
        if(this.displayName.equals(newDisplayName)) return; // throw an error here if we enforce 'change' in domain
        if(newDisplayName == null || newDisplayName.isBlank()) throw new IllegalArgumentException("Display name must not be blank");
        // check Sentence
        // trim()
        this.displayName = newDisplayName;
    }

    public void optInToMarketing() {
        this.marketingOptIn = true;
    }

    public void optOutOfMarketing() {
        this.marketingOptIn = false;
    }

    // ---- Internal helper methods ----

}

