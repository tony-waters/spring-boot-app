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

    Ticket(String description) {
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Description must not be empty");
        this.description = description;
        this.status = TicketStatus.OPEN;
    }

    public void updateStatus(TicketStatus newStatus) {
        if(newStatus == null) throw new IllegalArgumentException("New status must not be null");
        this.status = newStatus;
    }

    public Set<Tag> getTags() {
        // stop external modification that could break relationships
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

    void setCustomerInternal(Customer customer) {
        if (customer == null) throw new IllegalArgumentException("Ticket must have a Customer");
        if (this.customer != null && this.customer != customer) {
            throw new IllegalStateException("Ticket customer cannot be changed; delete and recreate instead");
        }
        this.customer = customer;
    }

    @Override
    public String toString() {
        return String.format(
                "SupportTicket[id=%d, description='%s']",
                getId(), description);
    }

}
