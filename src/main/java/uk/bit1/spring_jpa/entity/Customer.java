package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer extends BaseEntity {

    @Id
    @SequenceGenerator(name="global_seq", sequenceName="global_seq", allocationSize=50)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="global_seq")
    @Getter  // no setter by design
    private Long id;

    @Getter // no setter by design
    @OneToOne(
            mappedBy = "customer",
            // TODO: this is redundant?
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            optional = true
    )
    private Profile profile;

    // no public getter or setter for Collections by design
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

    // ---- Getters ----

    // TODO: what if there are 100s of Tickets? Leave this to Repositories?
//    public Set<Ticket> getTickets() {
//        // prevent external modification that could break relationships
//        return java.util.Collections.unmodifiableSet(tickets);
//    }

    // ---- Domain logic - Maintain relationship invariants for Customer -> Profile ----

    // Customer has control of Customer-Profile relationship changes (despite Profile being the Owner side)
    public void createProfile(String displayName, boolean marketingOptIn) {
        if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("Display name must not be null");
        if (this.profile != null) throw new IllegalStateException("Customer already has a Profile");
        Profile profile = new Profile(displayName, marketingOptIn);
        this.profile = profile;
        profile.setCustomerInternal(this);
    }

    public void removeProfile() {
        if (this.profile == null) return;
        Profile old = this.profile;
        this.profile = null;
        old.clearCustomerInternal();
    }

    // ---- Domain logic - Maintain relationship invariants for Customer -> Ticket ----

    // Customer has control of Customer-Ticket relationship changes (despite Ticket being the Owner side)
    public Ticket raiseTicket(String description) {
        if(description == null || description.isBlank()) throw new IllegalArgumentException("Description must not be null");
        // check Sentence
        // trim()
        Ticket ticket = new Ticket(description);
        addTicketInternal(ticket);
        return ticket;
    }

    public void removeTicket(Ticket ticket) {
        if(ticket == null) throw new IllegalArgumentException("Ticket must not be null");
        // Object comparison like "ticket.getCustomer() != this" will not work properly
        // with inherited BaseEntity.equals()/hashcode() as 'this' may be a Hibernate proxy
        // ... need to ensure use of 'equals()' method '!this.equals(customer)'
        if (!this.equals(ticket.getCustomer())) {
            throw new IllegalArgumentException("Ticket does not belong to this Customer");
        }
        removeTicketInternal(ticket);
    }

    public void removeAllTickets() {
        // Iterating over a copy avoids ConcurrentModificationException
        for (Ticket ticket : new HashSet<>(tickets)) {
            removeTicket(ticket);
        }
    }

    private void addTicketInternal(Ticket ticket) {
        Customer existing = ticket.getCustomer();
        // Object comparison like "<Entity> != this" will not work properly
        // with inherited BaseEntity.equals()/hashcode() as 'this' may be a Hibernate proxy
        // ... need to ensure use of 'equals()' method '!this.equals(existing)'
        if (existing != null && !this.equals(existing)) {
            throw new IllegalStateException("Cannot move Ticket between Customers. Delete and replace instead");
        }
        ticket.setCustomerInternal(this); // safe even if already set
        tickets.add(ticket);
    }

    private void removeTicketInternal(Ticket ticket) {
        Customer customer = ticket.getCustomer();
        boolean removed = tickets.remove(ticket);
        if (!removed) {
            throw new IllegalStateException("Ticket was not in Customer.tickets (detached instance?)");
        }
        // we do not null Ticket.customer to preserve 'nullable = false' in the Domain model.
        // orphanRemoval will delete on flush
        // ... will (should) be run within a @Transactional context in the Service layer
    }

    // ---- Domain logic - Maintain local state transition invariants ----

    public void changeName(String firstName, String lastName) {
        if(firstName == null || firstName.isBlank()) throw new IllegalArgumentException("firstName must have a value");
        if (lastName == null || lastName.isBlank()) throw new IllegalArgumentException("lastName must have a value");
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // ---- Internal helper methods ----

    // ---- General ----

    @Override
    public String toString() {
        return "Customer{id=" + getId() + ", firstName=" + firstName + ", lastName=" + lastName + "}";
    }

}
