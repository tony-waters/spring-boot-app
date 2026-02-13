package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer_profile")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

    @Id
    @Getter  // no setter by design
    private Long id;

    @Getter  // no setter by design
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    @Getter  // no setter by design
    @Column(name = "display_name", length = 80, nullable = false, unique = true)
    private String displayName;

    @Getter  // no setter by design
    @Column(name = "marketing_opt_in", nullable = false)
    private boolean marketingOptIn = false;

    // ---- Constructors ----

    public Profile(String displayName, boolean marketingOptIn) {
        if(displayName == null || displayName.isBlank()) throw new IllegalArgumentException("Display name must not be blank");
        this.displayName = displayName;
        this.marketingOptIn = marketingOptIn;
    }

    // ---- Domain methods ----

    public void changeDisplayName(String newDisplayName) {
        if(newDisplayName == null || newDisplayName.isBlank()) throw new IllegalArgumentException("Display name must not be blank");
        this.displayName = newDisplayName;
    }

    // ---- Internal helper methods ----

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
}

