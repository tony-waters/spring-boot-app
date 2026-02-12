package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer extends BaseEntity {

//    @OneToOne(
//            mappedBy = "customer",
//            cascade = CascadeType.ALL,
//            orphanRemoval = true
//    )
//    private ContactInfo contactInfo;

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

    public Customer(String lastName, String firstName) {
        this.lastName= lastName;
        this.firstName = firstName;
    }

    public Ticket createTicket(String description) {
        Ticket ticket = new Ticket(description);
        addTicketInternal(ticket);
        return ticket;
    }

    private void addTicketInternal(Ticket ticket) {
        if (ticket == null) throw new IllegalArgumentException("Ticket must not be null");

        // Object comparison like "ticket.getCustomer() != this" will not work properly
        // with inherited BaseEntity.equals()/hashcode() as 'this' may be a Hibernate proxy
        // ... need to force use of 'equals()' method '!this.equals(existing)'
        Customer existing = ticket.getCustomer();
        if (existing != null && !this.equals(existing)) {
            throw new IllegalStateException("Cannot move Ticket between Customers");
        }
        ticket.setCustomerInternal(this); // safe even if already set
        tickets.add(ticket);
    }

    public void removeTicketAndDelete(Ticket ticket) {
        if (ticket == null) return;

        // Object comparison like "ticket.getCustomer() != this" will not work properly
        // with inherited BaseEntity.equals()/hashcode() as 'this' may be a Hibernate proxy
        // ... need to force use of 'equals()' method '!this.equals(customer)'
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

    public Set<Ticket> getTickets() {
        // stop external modification that could break relationships
        return java.util.Collections.unmodifiableSet(tickets);
    }


//    public ContactInfo getContactInfo() {
//        return contactInfo;

//    }
//    public void setContactInfo(ContactInfo contactInfo) {
//        if (this.contactInfo != null) {
//            this.contactInfo.setCustomer(null);
//        }
//        this.contactInfo = contactInfo;
//        if (contactInfo != null) {
//            contactInfo.setCustomer(this);
//        }
//    }



    @Override
    public String toString() {
        return "Customer{id=" + getId() + ", firstName=" + firstName + ", lastName=" + lastName + "}";
    }

}
