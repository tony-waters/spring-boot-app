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
    @SequenceGenerator(name="global_seq", sequenceName="global_seq", allocationSize=50)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="global_seq")
    @Getter  // no setter by design
    private Long id;

    @Getter  // no setter by design
    @NotBlank
    @Size(min = 2, max = 50)
    @Column(name = "email_address", length = 50, nullable = false, unique = true)
    private String emailAddress;

    @Getter  // no setter by design
    @Column(name = "marketing_opt_in", nullable = false)
    private boolean marketingOptIn = false;

    // ---- Constructors ----

    // Customer controls lifecycle of Customer->Profile
    // so constructor hidden using package-private access
    // ... use Customer.createProfile() or Customer.attachProfile() instead
    Profile(String emailAddress, boolean marketingOptIn) {
        if(emailAddress == null || emailAddress.isBlank()) {
            throw new IllegalArgumentException("emailAddress must not be blank");
        }
        this.emailAddress = emailAddress.strip();
        this.marketingOptIn = marketingOptIn;
    }

    // ---- State transition ----

    public void changeEmailAddress(String newEmailAddress) {
        if(this.emailAddress.equals(newEmailAddress)) {
            return; // throw an error here if we enforce 'change' in domain
        }
        if(newEmailAddress == null || newEmailAddress.isBlank()) {
            throw new IllegalArgumentException("emailAddress must not be blank");
        }
        this.emailAddress = newEmailAddress.strip();
    }

    public void optInToMarketing() {
        this.marketingOptIn = true;
    }

    public void optOutOfMarketing() {
        this.marketingOptIn = false;
    }

}

