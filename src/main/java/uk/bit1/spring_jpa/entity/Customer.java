package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
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

    // Owning side
    @Getter // no setter by design
    @OneToOne(
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = CascadeType.ALL,
            optional = true
    )
    @MapsId
    @JoinColumn(name = "id") // Owning side is here
    private Profile profile;

    // parent / inverse side
    // unmodifiable getter below - no setter for Collection by design
    @OneToMany(
            mappedBy = "customer", // FK is in the Tickets table
            orphanRemoval = true,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private Set<Ticket> tickets = new HashSet<>();

    @Getter // no setter by design
    @NotBlank
    @Size(min = 2, max = 80)
    @Column(name = "display_name", length = 80, nullable = false)
    private String displayName;

    // ---- Constructors ----

    public Customer(String displayName, String firstName) {
        if(displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("lastName must have a value");
        }
        if(firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("firstName must have a value");
        }
        this.displayName = displayName.strip();
    }

    // ---- Collection getters ----

    Set<Ticket> getTickets() {
        return Collections.unmodifiableSet(tickets);
    }

    // ---- Customer -> Profile relationship ----

    // Customer has lifecycle control of Customer-Profile relationship
    public Profile createProfile(String displayName, boolean marketingOptIn) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Display name must not be null");
        }
        if (this.profile != null) {
            throw new IllegalStateException("Customer already has a Profile");
        }
        Profile profile = new Profile(displayName.strip(), marketingOptIn);
        this.profile = profile;
        return profile;
    }

    public void removeProfile() {
        if (this.profile == null) {
            throw new IllegalStateException("Customer has no Profile to remove");
        }
        Profile old = this.profile;
        this.profile = null;
    }

    // ---- Customer -> Ticket relationship ----

    // Customer has lifecycle control of Customer-Ticket relationship
    public Ticket raiseTicket(String description) {
        if(description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description must not be null");
        }
        Ticket ticket = new Ticket(description.strip());
        addTicketInternal(ticket);
        return ticket;
    }

    public void removeTicket(Ticket ticket) {
        if(ticket == null) {
            throw new IllegalArgumentException("Ticket must not be null");
        }
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

    // ---- State transition ----

    public void changeName(String firstName, String lastName) {
        if(firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("firstName must have a value");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("lastName must have a value");
        }
//        this.firstName = firstName.strip();
        this.displayName = lastName.strip();
    }

    // ---- General ----

    @Override
    public String toString() {
        return "Customer{id=" + getId()  + ", lastName=" + displayName + "}";
    }

}
