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
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            optional = true
    )
    @JoinColumn( // Owning side is here
            name = "profile_id",
            unique = true // enforce 1-1 in DB
    )
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

    public Customer(String displayName) {
        if(displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must have a value");
        }
        this.displayName = displayName.strip();
    }

    // ---- Collection getters ----

    Set<Ticket> getTickets() {
        return Collections.unmodifiableSet(tickets);
    }

    // ---- Customer -> Profile relationship ----

    // Customer has lifecycle control of Customer-Profile relationship
    public Profile createProfile(String emailAddress, boolean marketingOptIn) {
        if (emailAddress == null || emailAddress.isBlank()) {
            throw new IllegalArgumentException("emailAddress must not be null");
        }
        if (this.profile != null) {
            throw new IllegalStateException("Customer already has a Profile");
        }
        Profile profile = new Profile(emailAddress.strip(), marketingOptIn);
        this.profile = profile;
        return profile;
    }

    public void attachProfile(Profile profile) {
        if(profile == null) {
            throw new IllegalArgumentException("profile must not be null");
        }
        if (this.profile != null) {
            throw new IllegalStateException("Customer already has a Profile");
        }
        this.profile = profile;
    }

    public void removeProfile() {
        if (this.profile == null) {
            throw new IllegalStateException("Customer has no Profile to remove");
        }
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
        // orphanRemoval will delete on flush anyway
        ticket.removeCustomerInternal();
    }

    // ---- State transition ----

    public void changeDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must have a value");
        }
        this.displayName = displayName.strip();
    }

    // ---- General ----

    @Override
    public String toString() {
        return "Customer{id=" + getId()  + ", displayName=" + displayName + "}";
    }

}
