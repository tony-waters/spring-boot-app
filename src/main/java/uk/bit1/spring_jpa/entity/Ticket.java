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

    // TODO: should 'customer' have a public getter?
    @Getter // no setter by design
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // getter below, no setter by design
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "ticket_tag",
            joinColumns = @JoinColumn(name = "ticket_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Getter // no setter by design
    @NotBlank
    @Size(min = 2, max = 255)
    @Column(nullable = false, length = 255)
    private String description;

    @Getter // no setter by design
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    // ---- Constructors ----

    Ticket(String description) {
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Description must not be empty");
        this.description = description;
        this.status = TicketStatus.NEW;
    }

    // ---- Getters ----

    public Set<Tag> getTags() {
        // prevent external modification that could break relationships
        return java.util.Collections.unmodifiableSet(tags);
    }

    // ---- Domain logic - Maintain relationship invariants for Ticket -> Customer ----

        // public access to relationship handled by Customer entity

    void setCustomerInternal(Customer customer) {
        if (customer == null) throw new IllegalArgumentException("Ticket must have a Customer");

        // Object comparison like "this.customer != customer" will not work properly
        // with inherited BaseEntity.equals()/hashcode() as 'this' may be a Hibernate proxy
        // ... need to ensure use of 'equals()' method '!this.customer.equals(customer)'
        if (this.customer != null && !this.customer.equals(customer)) {
            throw new IllegalStateException("Ticket customer cannot be changed; delete and recreate instead");
        }
        this.customer = customer;
    }

    // ---- Domain logic - Maintain relationship invariants for Ticket -> Tag ----

    // TODO: check unique
    public void addTag(Tag tag) {
        if (tag == null) return;
        if (tags.add(tag)) {
            tag.addTicketInternal(this);
        }
    }

    // TODO: check exists
    public void removeTag(Tag tag) {
        if (tag == null) return;
        if (tags.remove(tag)) {
            tag.removeTicketInternal(this);
        }
    }

    public void clearTags() {
        for (Tag tag : new HashSet<>(tags)) {
            removeTag(tag);
        }
    }

    // ---- Domain logic - Maintain local state transition invariants ----

    public void updateTicket(String newDescription) {
        if(newDescription == null || newDescription.isBlank()) throw new IllegalArgumentException("Description must not be empty");
        this.description = newDescription;
    }

    // TODO:
    // 3) Ticket status logic: decent, but you’re missing transitions
    //
    //You have resolveTicket() and closeTicket(). But you can’t move from:
    //
    //NEW -> OPEN
    //
    //OPEN -> IN_PROGRESS
    //
    //RESOLVED -> CLOSED (maybe implicit)
    //
    //If you mean NEW is a thing, you need methods like:
    //
    //start() or open()
    //
    //beginWork()
    //
    //reopen() (if allowed)
    //
    //Otherwise “NEW” is pointless and will trap you in invalid states.
    //
    //Also: throw IllegalStateException for invalid transitions (not IllegalArgumentException). Invalid state is not invalid input.

    public void resolveTicket() {
        if(isClosed()) {
            throw new IllegalStateException("Cannot Resolve a closed Ticket");
        }
        if(isNew()) {
            throw new IllegalArgumentException("Cannot Resolve a new Ticket, must be in open or in-progress state");
        }
        this.status = TicketStatus.RESOLVED;
    }

    public void closeTicket() {
        if(isClosed()) {
            throw new IllegalStateException("Ticket is already closed");
        }
        if(isNew()) {
            throw new IllegalArgumentException("Ticket cannot be closed when new");
        }
        this.status = TicketStatus.CLOSED;
    }

    // ---- Internal helper methods ----

    private boolean isClosed() {
        return this.status == TicketStatus.CLOSED;
    }

    private boolean isNew() {
        return this.status == TicketStatus.NEW;
    }


    // ---- General ----

    @Override
    public String toString() {
        return String.format(
                "SupportTicket[id=%d, description='%s']",
                getId(), description);
    }

}
