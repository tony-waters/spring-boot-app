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
@Table(
        name = "ticket",
        indexes = @Index(name = "idx_ticket_customer_id", columnList = "customer_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket extends BaseEntity {

    @Id
    @SequenceGenerator(name="global_seq", sequenceName="global_seq", allocationSize=50)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="global_seq")
    @Getter  // no setter by design
    private Long id;

    @Getter(AccessLevel.PACKAGE) // no setter by design
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn( // Owning side is here
            name = "customer_id",
            nullable = false
    )
    private Customer customer;

    // unmodifiable getter below, no setter by design
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable( // Owning side is here
            name = "ticket_tag",
            joinColumns = @JoinColumn(name = "ticket_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Getter // no setter by design
    @NotBlank
    @Size(min = 10, max = 255)
    @Column(nullable = false, length = 255)
    private String description;

    @Getter // no setter by design
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    // ---- Constructors ----

    // Ticket must not exist independently of Customer
    // so constructor hidden using package-private access
    // ... use Customer.raiseTicket() instead
    Ticket(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        String stripped =  description.strip();
        if(stripped.isBlank() || stripped.length() <10) {
            throw new IllegalArgumentException("Description must be at least 10 characters");
        }
        this.description = stripped;
        this.status = TicketStatus.OPEN;
    }

    // ---- Collection getters ----

    Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    // ---- Ticket -> Customer relationship ----
    // (public access to relationship handled by Customer entity)

    void setCustomerInternal(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Ticket must have a Customer");
        }
        if (this.customer != null && !this.customer.equals(customer)) {
            throw new IllegalStateException("Ticket customer cannot be changed - delete and recreate instead");
        }
        this.customer = customer;
    }

    void removeCustomerInternal() {
        this.customer = null;
    }

    // ---- Ticket -> Tag relationship ----

    public void addTag(Tag tag) {
        requireEditable("addTag");
        if (tag == null) return; // addTag should be idempotent
        if (tags.add(tag)) {
            tag.addTicketInternal(this);
        }
    }

    public void removeTag(Tag tag) {
        requireEditable("removeTag");
        if (tag == null) return; // removeTag should be idempotent
        if (tags.remove(tag)) {
            tag.removeTicketInternal(this);
        }
    }

    public void clearTags() {
        requireEditable("clearTags");
        for (Tag tag : new HashSet<>(tags)) {
            removeTag(tag);
        }
    }

    // ---- State transition ----

    public void changeDescription(String description) {
        requireEditable("changeDescription");
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        String stripped =  description.strip();
        if(stripped.isBlank() || stripped.length() <10) {
            throw new IllegalArgumentException("Description must be at least 10 characters");
        }
        this.description = stripped;
    }

    public void startWork() {
        transitionTo(TicketStatus.IN_PROGRESS, "startWork", TicketStatus.OPEN);
    }

    public void resolve() {
        transitionTo(TicketStatus.RESOLVED, "resolve", TicketStatus.OPEN, TicketStatus.IN_PROGRESS);
    }

    public void reopen() {
        transitionTo(TicketStatus.OPEN, "reopen", TicketStatus.RESOLVED);
    }

    public void close() {
        transitionTo(TicketStatus.CLOSED, "close", TicketStatus.RESOLVED);
    }

    private void requireNotClosed(String action) {
        if (status == TicketStatus.CLOSED)
            throw new IllegalStateException("Cannot " + action + " when ticket is CLOSED");
    }

    private void requireEditable(String action) {
        requireNotClosed(action);
        if (status == TicketStatus.RESOLVED)
            throw new IllegalStateException("Cannot " + action + " when ticket is RESOLVED");
    }

    private void transitionTo(TicketStatus target, String action, TicketStatus... allowedFrom) {
        requireNotClosed(action);
        for (TicketStatus s : allowedFrom) {
            if (status == s) {
                status = target;
                return;
            }
        }
        throw new IllegalStateException("Cannot " + action + " when ticket is " + status);
    }

    // ---- General ----

    @Override
    public String toString() {
        return String.format(
                "Ticket[id=%d, description='%s']",
                getId(), description);
    }

}
