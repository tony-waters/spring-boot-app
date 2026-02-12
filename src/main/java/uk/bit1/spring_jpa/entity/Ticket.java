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
        this.status = TicketStatus.OPEN;
    }

    // ---- Domain methods ----

    public void updateTicket(TicketStatus newStatus, String newDescription) {
        if(newStatus == null) throw new IllegalArgumentException("New status must not be null");
        if(newDescription == null || newDescription.isBlank()) throw new IllegalArgumentException("Description must not be empty");
        this.status = newStatus;
        this.description = newDescription;
    }

    public Set<Tag> getTags() {
        // prevent external modification that could break relationships
        return java.util.Collections.unmodifiableSet(tags);
    }

    public void addTag(Tag tag) {
        if (tag == null) return;
        if (tags.add(tag)) {
            tag.addTicketInternal(this);
        }
    }
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

    // ---- Internal helper methods ----

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

    // ---- General ----

    @Override
    public String toString() {
        return String.format(
                "SupportTicket[id=%d, description='%s']",
                getId(), description);
    }

}
