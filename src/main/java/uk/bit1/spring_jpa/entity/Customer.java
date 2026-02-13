package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer extends BaseEntity {

    @Id
    @SequenceGenerator(name="global_seq", sequenceName="global_seq", allocationSize=50)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="global_seq")
    @Getter  // no setter by design
    private Long id;

    // getter below, no setter by design
    @OneToOne(
            mappedBy = "customer",
            // TODO: this is redundant?
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            optional = true
    )
    private Profile profile;

    // getter below, no setter by design
    @OneToMany(
            mappedBy = "customer",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<Ticket> tickets = new HashSet<>();

    @Getter // no setter by design
    @NotBlank
    @Size(min = 2, max = 50)
    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;

    @Getter // no setter by design
    @NotBlank
    @Size(min = 2, max = 50)
    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    // ---- Constructors ----

    public Customer(String lastName, String firstName) {
        if(lastName == null || lastName.isBlank()) throw new IllegalArgumentException("lastName must have a value");
        if(firstName == null || firstName.isBlank()) throw new IllegalArgumentException("firstName must have a value");
        this.lastName= lastName;
        this.firstName = firstName;
    }

    // ---- Domain methods ----

    public Optional<Profile> getProfile() {
        return Optional.ofNullable(profile);
    }

    public void createProfile(String displayName, boolean marketingOptIn) {
        if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("Display name must not be null");
        if (this.profile != null) throw new IllegalStateException("Customer already has a Profile");
        Profile profile = new Profile(displayName, marketingOptIn);
        this.profile = profile;
        profile.setCustomerInternal(this);
    }

    public void changeProfileDisplayName(String newDisplayName) {
        requireProfile().changeDisplayName(newDisplayName);
    }

    public void setProfileMarketingOptIn(boolean optIn) {
        if (optIn) {
            requireProfile().optInToMarketing();
        } else {
            requireProfile().optOutOfMarketing();
        }
    }

    public void deleteProfile() {
        if (this.profile == null) return;
        Profile old = this.profile;
        this.profile = null;
        old.clearCustomerInternal();
    }

    public Set<Ticket> getTickets() {
        // prevent external modification that could break relationships
        return java.util.Collections.unmodifiableSet(tickets);
    }

    public Ticket createTicket(String description) {
        Ticket ticket = new Ticket(description);
        addTicketInternal(ticket);
        return ticket;
    }

    public void removeTicketAndDelete(Ticket ticket) {
        if (ticket == null) return;

        // Object comparison like "ticket.getCustomer() != this" will not work properly
        // with inherited BaseEntity.equals()/hashcode() as 'this' may be a Hibernate proxy
        // ... need to ensure use of 'equals()' method '!this.equals(customer)'
        Customer customer = ticket.getCustomer();
        if (!this.equals(customer)) {
            throw new IllegalArgumentException("Ticket does not belong to this Customer");
        }
        boolean removed = tickets.remove(ticket);
        if (!removed) {
            throw new IllegalStateException("Ticket was not in Customer.tickets (detached instance?)");
        }
        // we do not null Ticket.customer as 'nullable = false'
        // orphanRemoval will delete on flush
    }

    public void deleteAllTickets() {
        // Iterating over a copy avoids ConcurrentModificationException
        for (Ticket ticket : new HashSet<>(tickets)) {
            removeTicketAndDelete(ticket);
        }
    }

    // ---- Internal helper methods ----

    private Profile requireProfile() {
        if (this.profile == null) throw new IllegalStateException("Customer does not have a Profile");
        return this.profile;
    }

    private void addTicketInternal(Ticket ticket) {
        if (ticket == null) throw new IllegalArgumentException("Ticket must not be null");

        // Object comparison like "<Entity> != this" will not work properly
        // with inherited BaseEntity.equals()/hashcode() as 'this' may be a Hibernate proxy
        // ... need to ensure use of 'equals()' method '!this.equals(existing)'
        Customer existing = ticket.getCustomer();
        if (existing != null && !this.equals(existing)) {
            throw new IllegalStateException("Cannot move Ticket between Customers");
        }
        ticket.setCustomerInternal(this); // safe even if already set
        tickets.add(ticket);
    }

    // ---- General ----

    @Override
    public String toString() {
        return "Customer{id=" + getId() + ", firstName=" + firstName + ", lastName=" + lastName + "}";
    }

}
