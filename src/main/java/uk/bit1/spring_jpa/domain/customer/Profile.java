package uk.bit1.spring_jpa.domain.customer;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer_profile")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Profile {

    @Id
    @SequenceGenerator(name = "global_seq", sequenceName = "global_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_seq")
    @Getter
    private Long id;

    @Getter
    @Column(name = "email_address", length = 50, nullable = false, unique = true)
    private String emailAddress;

    @Getter
    @Column(name = "marketing_opt_in", nullable = false)
    private boolean marketingOptIn;

    Profile(String emailAddress, boolean marketingOptIn) {
        this.emailAddress = validateEmailAddress(emailAddress);
        this.marketingOptIn = marketingOptIn;
    }

    void changeEmailAddress(String newEmailAddress) {
        this.emailAddress = validateEmailAddress(newEmailAddress);
    }

    void optInToMarketing() {
        this.marketingOptIn = true;
    }

    void optOutOfMarketing() {
        this.marketingOptIn = false;
    }

    private static String validateEmailAddress(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("emailAddress must not be blank");
        }
        return value.strip();
    }
}